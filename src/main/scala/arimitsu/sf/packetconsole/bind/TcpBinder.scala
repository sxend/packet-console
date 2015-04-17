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
    case b@Bound(localAddress) =>
      context.system.log.info(s"starting: ${from.getHostName}:${from.getPort} -> ${to.getHostName}:${to.getPort}")
    case c@Connected(remote, local) =>
      val connection = sender()
      val inbound = context.actorOf(Props(classOf[Inbound], to, connection))
      connection ! Register(inbound)
  }
}
class Inbound(to: InetSocketAddress, connection: ActorRef) extends Actor{
  import context.system
  IO(Tcp) ! Tcp.Connect(to)
  override def receive = {
    case CommandFailed(_: Bind) => context stop self
    case PeerClosed     => context stop self
    case c @ Connected(remote, local) =>
      val outbound = context.actorOf(Props(classOf[Outbound], connection))
      connection ! Register(self)
      context become {
        case data: ByteString =>
          outbound ! Write(data)
        case CommandFailed(w: Write) =>
          context stop self
        case Received(data) =>
          outbound ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          context stop self
      }
  }
}
class Outbound(connection: ActorRef) extends Actor{
  override def receive = {
    case CommandFailed(_: Bind) => context stop self
    case PeerClosed     => context stop self
    case Received(data) => connection ! data
  }
}

