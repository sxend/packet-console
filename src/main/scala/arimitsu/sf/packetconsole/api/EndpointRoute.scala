package arimitsu.sf.packetconsole.api

import akka.http.model.StatusCodes._
import akka.http.model._
import akka.http.server.Directives._
import akka.http.server.Route

object EndpointRoute {
  def apply(components: {}): Route = pathPrefix("api") {
    path("status") {
      get {
        complete(HttpResponse(OK, entity = "it works."))
      }
    }
  }
}
