package arimitsu.sf.packetconsole.bind

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import arimitsu.sf.packetconsole.data.Node

class TcpBinder(id: String, from: Node, to: Node) extends Actor with ActorLogging {

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
        case "stop" =>
          exchange ! "stop"
          context stop self
      }
    case _ => throw new UnsupportedOperationException("unknown message")
  }
}

class Exchange(inbound: ActorRef, to: Node) extends Actor with ActorLogging {
  val outbound = context.actorOf(Props(classOf[Outbound], inbound, to))

  override def receive = {
    case Received(data) => outbound ! data
    case data: ByteString => inbound ! Write(data)
    case PeerClosed => context stop self
    case "stop" =>
      outbound ! "stop"
      context stop self
    case _ => throw new UnsupportedOperationException("unknown message")
  }
}

class Outbound(inbound: ActorRef, to: Node) extends Actor with ActorLogging {

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
        case "stop" => context stop self
        case a: Any => throw new UnsupportedOperationException(s"unknown message $a")
      }
  }
}

