package arimitsu.sf.packetconsole.api

import akka.actor.ActorSystem
import akka.http.model.StatusCodes._
import akka.http.model._
import akka.http.server.Directives._
import akka.http.server.Route

class RequestMapping(components: {
  val system: ActorSystem
  val bindsHandler: BindsHandler
  val protocolsHandler: ProtocolsHandler
  val statisticsHandler: StatisticsHandler
}) {

  import components._

  def route: Route = get {
    path("status")(complete(HttpResponse(OK, entity = "it works"))) ~
      path("binds" / Rest)(bindsHandler.get) ~
      path("binds")(bindsHandler.list) ~
      path("protocols")(protocolsHandler.list) ~
      path("statistics")(statisticsHandler.get)
  } ~ put {
    path("binds" / Segment / Segment / Segment)(bindsHandler.register)
  } ~ delete {
    path("binds" / Rest)(bindsHandler.delete)
  }

}
