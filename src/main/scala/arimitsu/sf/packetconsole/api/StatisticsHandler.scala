package arimitsu.sf.packetconsole.api

import akka.http.model.HttpResponse
import akka.http.model.StatusCodes._
import akka.http.server.Directives._
import arimitsu.sf.packetconsole.bind.BindManager

import scala.util.Failure

class StatisticsHandler(components: {
  val bindManager: BindManager
}) {
  lazy val bindManager = components.bindManager

  def get = {
    onComplete(bindManager.statistics) {
      case util.Success(stat) => complete(HttpResponse(OK, entity = stat))
      case Failure(t) => failWith(t)
    }
  }
}
