package arimitsu.sf.packetconsole.api

import akka.actor.ActorSystem
import akka.http.model.StatusCodes._
import akka.http.model._
import akka.http.model.headers._
import akka.http.server.Directives._
import akka.http.server.Route
import arimitsu.sf.packetconsole.PropertyKey

class RequestMapping(components: {
  val system: ActorSystem
  val bindsHandler: BindsHandler
  val protocolsHandler: ProtocolsHandler
  val statisticsHandler: StatisticsHandler
}) {

  import components._

  def route: Route = validate {
    get {
      path("status")(complete(HttpResponse(OK, entity = "it works"))) ~
        path("binds" / Rest)(bindsHandler.get) ~
        path("binds")(bindsHandler.list) ~
        path("protocols")(protocolsHandler.list) ~
        path("statistics")(statisticsHandler.get)
    } ~ put {
      path("binds")(parameters('protocol, 'from, 'to)(bindsHandler.register))
    } ~ delete {
      path("binds")(parameter('id)(bindsHandler.delete))
    }
  }

  private def validate(r: => Route) = parameter('credential) {
    case c if c == credential => pathPrefix("api") {
      respondWithHeaders(Server("packet-console"))(r)
    }
    case _ => reject
  }

  private val credential = {
    Option(System.getProperty(PropertyKey.PC_API_CREDENTIAL)) match {
      case Some(c) => c
      case None =>
        val e = new RuntimeException("credential option is require!!!")
        system.log.error(e, "")
        throw e
    }
  }

}
