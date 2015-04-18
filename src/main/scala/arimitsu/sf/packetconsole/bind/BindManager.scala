package arimitsu.sf.packetconsole.bind

import java.util.UUID

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import arimitsu.sf.packetconsole.data.{Bind, Node}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

class BindManager(components: {
  val system: ActorSystem
}) {

  implicit val timeout = Timeout(3 seconds)

  private val protocols = Array("tcp", "udp")

  def start() = {
    managementActor
  }

  lazy val managementActor = components.system.actorOf(Props(classOf[BindManagementActor], components))

  def bind(protocol: String, from: Node, to: Node): Future[Bind] = {
    managementActor.ask((protocol, from, to)).mapTo[Bind]
  }

  def getBind(id: String): Future[Option[Bind]] = {
    managementActor.ask(id).mapTo[Option[Bind]]
  }

  def getBindList: Future[List[Bind]] = {
    managementActor.ask("list").mapTo[List[Bind]]
  }

  def statistics: Future[String] = {
    managementActor.ask("statistics").mapTo[String]
  }

  def unbound(id: String): Future[Unit] = {
    managementActor.ask(("delete", id)).mapTo[Unit]
  }

  def supportedProtocols: Array[String] = protocols
}

private[bind] class BindManagementActor(components: {

}) extends Actor with ActorLogging {
  import Binder.Protocol._
  private val bindMap = new mutable.HashMap[String, Bind]()
  private val actorMap = new mutable.HashMap[String, ActorRef]()

  override def receive = {
    case (protocol: String, from: Node, to: Node) => {
      protocol match {
        case p@"tcp" =>
          val id = UUID.randomUUID().toString
          val actor = context.actorOf(Props(classOf[TcpBinder], id, from, to))
          val bind = Bind(id, p, from, to)
          bindMap.put(id, bind)
          actorMap.put(id, actor)
          sender() ! bind
        case a: Any => throw new UnsupportedOperationException(s"unknown message $a")
      }
    }
    case "list" =>
      sender() ! bindMap.values.toList
    case ("delete", id: String) =>
      bindMap.remove(id)
      actorMap.remove(id) match {
        case Some(a) => a ! Stop
      }
      sender() !()
    case s@"statistics" =>
      sender() ! "statistics"
    case id: String =>
      sender() ! bindMap.get(id)
    case a: Any => throw new UnsupportedOperationException(s"unknown message $a")
  }
}