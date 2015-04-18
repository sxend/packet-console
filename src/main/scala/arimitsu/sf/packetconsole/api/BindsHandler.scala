package arimitsu.sf.packetconsole.api

import akka.http.model.HttpResponse
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.server.Route
import arimitsu.sf.packetconsole.bind.BindManager
import arimitsu.sf.packetconsole.data.{Node, Bind}
import arimitsu.sf.packetconsole.api.JsonFormats._
import spray.json.DefaultJsonProtocol._
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
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

  def register(protocol: String, from: String = "", to: String = "") = {
    val fromNode = {
      val arr = from.split(":")
      Node(arr.head, arr.last.toInt)
    }
    val toNode = {
      val arr = to.split(":")
      Node(arr.head, arr.last.toInt)
    }
    onComplete(bindManager.bind(protocol, fromNode, toNode)) {
      case util.Success(s) => complete(HttpResponse(OK, entity = s.toString))
      case Failure(t) => failWith(t)
    }
  }
}
