package arimitsu.sf.packetconsole.bind

import java.util.UUID

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import arimitsu.sf.packetconsole.{Protocol, PacketConsoleException}
import arimitsu.sf.packetconsole.Protocol.{UDP, TCP}
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

  import Binder.Message._
  import BindManagementActor.Message._
  private val bindings = new mutable.HashMap[String, Bind]()
  private val actors = new mutable.HashMap[String, ActorRef]()

  override def receive = {
    case Get(id) => sender() ! bindings.get(id)
    case List => sender() ! bindings.values.toList
    case Register(protocol, from, to) => {
      protocol match {
        case TCP =>
          val id = UUID.randomUUID().toString
          val actor = context.actorOf(Props(classOf[TcpBinder], id, from, to))
          val bind = Bind(id, TCP, from, to)
          bindings.put(id, bind)
          actors.put(id, actor)
          sender() ! bind
        case any: Any => throw new PacketConsoleException(s"unsupported protocol $any")
      }
    }

    case Delete(id) =>
      bindings.remove(id)
      actors.remove(id) match {
        case Some(a) => a ! Stop
      }
      sender() !()
    case Statistics =>
      sender() ! "statistics"

    case any: Any => throw new PacketConsoleException(s"unknown message $any")
  }
}

object BindManagementActor {

  object Message {

    case object Protocols

    case object Statistics

    case object List

    case class Delete(id: String)

    case class Get(id: String)

    case class Register(protocol: Protocol, from: Node, to: Node)

  }

}