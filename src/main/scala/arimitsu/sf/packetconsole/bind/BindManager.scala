package arimitsu.sf.packetconsole.bind

import java.util.UUID

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import arimitsu.sf.packetconsole.Protocol.TCP
import arimitsu.sf.packetconsole.bind.BindManagementActor.Message
import arimitsu.sf.packetconsole.data.{Bind, Node}
import arimitsu.sf.packetconsole.{PacketConsoleException, Protocol}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

class BindManager(components: {
  val system: ActorSystem
}) {
  import Message._

  implicit val timeout = Timeout(3 seconds)

  def start() = {
    managementActor
  }

  lazy val managementActor = components.system.actorOf(Props(classOf[BindManagementActor], components))

  def bind(protocol: Protocol, from: Node, to: Node): Future[Bind] = {
    managementActor.ask(Register(protocol, from, to)).mapTo[Bind]
  }

  def getBind(id: String): Future[Option[Bind]] = {
    managementActor.ask(Get(id)).mapTo[Option[Bind]]
  }

  def getBindList: Future[List[Bind]] = {
    managementActor.ask(Message.List).mapTo[List[Bind]]
  }

  def statistics: Future[String] = {
    managementActor.ask(Statistics).mapTo[String]
  }

  def unbound(id: String): Future[Unit] = {
    managementActor.ask(Delete(id)).mapTo[Unit]
  }

  def supportedProtocols: List[String] = Protocol.list.map(_.name)
}

private[bind] class BindManagementActor(components: {

}) extends Actor with ActorLogging {

  import BindManagementActor.Message._
  import Binder.Message._

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

    case object Statistics

    case object List

    case class Delete(id: String)

    case class Get(id: String)

    case class Register(protocol: Protocol, from: Node, to: Node)

  }

}