package arimitsu.sf.packetconsole.api

import akka.http.model.HttpResponse
import akka.http.model.StatusCodes._
import akka.http.server.Directives._

class ProtocolsHandler(components: {

}) {
  def list = complete(HttpResponse(OK, entity = "GET /api/protocols"))
}
