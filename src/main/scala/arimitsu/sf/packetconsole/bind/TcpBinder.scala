package arimitsu.sf.packetconsole.bind

import java.net.InetSocketAddress

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{Tcp, IO}
import Tcp._
import akka.util.ByteString

class TcpBinder(from: InetSocketAddress, to: InetSocketAddress) extends Actor {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, from)

  override def receive = {
    case Bound(localAddress) =>
      context.system.log.info(s"starting: ${from.getHostName}:${from.getPort} -> ${to.getHostName}:${to.getPort}")
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, local) =>
      val inbound = sender()
      val exchange = context.actorOf(Props(classOf[Exchange], inbound, to))
      inbound ! Register(exchange)
  }
}

class Exchange(inbound: ActorRef, to: InetSocketAddress) extends Actor {
  val outbound = context.actorOf(Props(classOf[Outbound], inbound, to))

  override def receive = {
    case Received(data) => outbound ! data
    case data: ByteString => inbound ! Write(data)
    case PeerClosed => context stop self
  }
}

class Outbound(inbound: ActorRef, to: InetSocketAddress) extends Actor {
  IO(Tcp) ! Tcp.Connect(to)

  override def receive = {
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, local) =>
      val outbound = sender()
      outbound ! Register(self)
      context.become {
        case Received(data) => inbound ! data
        case data: ByteString => outbound ! Write(data)
      }
  }
}

