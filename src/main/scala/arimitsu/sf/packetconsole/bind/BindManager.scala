package arimitsu.sf.packetconsole.bind

import java.net.InetSocketAddress
import java.util.UUID

import akka.actor.{Actor, Props, ActorSystem}
import arimitsu.sf.packetconsole.data.{Node, Bind}

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.Future

class BindManager(components: {
  val system: ActorSystem
}) {
  implicit val timeout = Timeout(3 seconds)

  private val protocols = Array("tcp", "udp")

  def start() = {
    managementActor
  }

  lazy val managementActor = components.system.actorOf(Props(classOf[BindManagementActor], components))

  def bind(protocol: String, from: InetSocketAddress, to: InetSocketAddress): Future[Bind] = {
    managementActor.ask((protocol, from, to)).mapTo[Bind]
  }

  def getBind(id: String): Future[Option[Bind]] = {
    managementActor.ask(id).mapTo[Option[Bind]]
  }

  def statistics: Future[String] = {
    managementActor.ask("statistics").mapTo[String]
  }

  def unbound(id: String): Future[Unit] = {
    ???
  }

  def supportedProtocols: Array[String] = protocols
}

private[bind] class BindManagementActor(components: {

}) extends Actor {
  private val bindMap = new mutable.HashMap[String, Bind]()

  override def receive = {
    case (protocol: String, from: Node, to: Node) => {
      protocol match {
        case p@"tcp" =>
          val id = UUID.randomUUID().toString
          context.actorOf(Props(classOf[TcpBinder], id, from, to))
          val bind = Bind(p, from, to)
          bindMap.put(id, bind)
          sender() ! bind
        case _ => ???
      }
    }
    case s@"statistics" =>
      sender() ! "statistics"
    case id: String =>
      sender() ! bindMap.get(id)
  }
}