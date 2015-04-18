package arimitsu.sf.packetconsole.api

import akka.http.model.HttpResponse
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import akka.http.server.Route
import arimitsu.sf.packetconsole.data.Bind

class BindsHandler(components: {

}) {
  def get(id: String) = complete(HttpResponse(OK, entity = "GET /api/binds/{id}"))

  def list = complete(HttpResponse(OK, entity = "GET /api/binds"))

  def delete(id: String) = complete(HttpResponse(OK, entity = "DELETE /api/binds/{id}"))

  def register(protocol: String, from: String, to: String) = complete(HttpResponse(OK, entity = "PUT /api/binds/{protocol}/{from_host}:{from_port}/{to_host}:{to_port}"))

}
