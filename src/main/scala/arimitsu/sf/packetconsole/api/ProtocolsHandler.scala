package arimitsu.sf.packetconsole.api

import akka.http.model.HttpResponse
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import arimitsu.sf.packetconsole.bind.BindManager

class ProtocolsHandler(components: {
  val bindManager: BindManager
}) {
  lazy val bindManager = components.bindManager
  def list = {
    val protocols = bindManager.supportedProtocols
    complete(HttpResponse(OK, entity = protocols.toList.toString))
  }
}
