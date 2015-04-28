package arimitsu.sf.packetconsole.api

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import arimitsu.sf.packetconsole.Protocol
import arimitsu.sf.packetconsole.bind.BindManager
import arimitsu.sf.packetconsole.data.{Node, Bind}
import arimitsu.sf.packetconsole.api.JsonFormats._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import spray.json.JsObject

import scala.concurrent.Future
import scala.util.Failure

class BindsHandler(components: {
  val bindManager: BindManager
}) {
  lazy val bindManager = components.bindManager

  def get(id: String) = {
    onComplete(bindManager.getBind(id)) {
      case util.Success(opt) =>
        opt match {
          case Some(a) => complete(HttpResponse(OK, entity = a.toString))
          case None => complete(HttpResponse(OK, entity = ""))
        }
      case Failure(t) => failWith(t)
    }
  }

  def list = onComplete(bindManager.getBindList){
    case util.Success(s) => complete(HttpResponse(OK, entity = s.toString))
    case Failure(t) => failWith(t)
  }

  def delete(id: String) = {
    onComplete(bindManager.unbound(id)) {
      case util.Success(s) => complete(HttpResponse(OK, entity = s.toString))
      case Failure(t) => failWith(t)
    }
  }

  def register(proto: String, f: String = "", t: String = "") = {
    val from = {
      val arr = f.split(":")
      Node(arr.head, arr.last.toInt)
    }
    val to = {
      val arr = t.split(":")
      Node(arr.head, arr.last.toInt)
    }
    val protocol = Protocol.valueOf(proto)
    onComplete(bindManager.bind(protocol, from, to)) {
      case util.Success(s) => complete(HttpResponse(OK, entity = s.toString))
      case Failure(t) => failWith(t)
    }
  }
}
