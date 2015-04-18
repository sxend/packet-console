package arimitsu.sf.packetconsole.bind

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import arimitsu.sf.packetconsole.PacketConsoleException
import arimitsu.sf.packetconsole.data.Node

class TcpBinder(id: String, from: Node, to: Node) extends Actor with ActorLogging {

  import Binder.Protocol._
  import context.system

  IO(Tcp) ! Tcp.Bind(self, from.toInet)

  override def receive = {
    case Bound(localAddress) =>
      log.info(s"starting: ${from.host}:${from.port} -> ${to.host}:${to.port}")
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, local) =>
      val inbound = sender()
      val exchange = context.actorOf(Props(classOf[Exchange], inbound, to))
      inbound ! Register(exchange)
      context.become {
        case Stop =>
          exchange ! Stop
          context stop self
      }
    case any: Any => throw new PacketConsoleException(s"unknown message : $any")
  }
}

class Exchange(inbound: ActorRef, to: Node) extends Actor with ActorLogging {

  import Binder.Protocol._

  val outbound = context.actorOf(Props(classOf[Outbound], inbound, to))

  override def receive = {
    case Received(data) => outbound ! data
    case data: ByteString => inbound ! Write(data)
    case PeerClosed => context stop self
    case Stop =>
      outbound ! Stop
      context stop self
    case any: Any => throw new PacketConsoleException(s"unknown message : $any")
  }
}

class Outbound(inbound: ActorRef, to: Node) extends Actor with ActorLogging {

  import Binder.Protocol._
  import context.system

  IO(Tcp) ! Tcp.Connect(to.toInet)

  override def receive = {
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, local) =>
      val outbound = sender()
      outbound ! Register(self)
      context.become {
        case Received(data) => inbound ! data
        case data: ByteString => outbound ! Write(data)
        case Stop => context stop self
        case any: Any => throw new PacketConsoleException(s"unknown message : $any")
      }
  }
}

