package arimitsu.sf.packetconsole.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http._
import akka.http.model.HttpRequest
import akka.http.server._
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink
import arimitsu.sf.packetconsole.PropertyKey
import arimitsu.sf.packetconsole.bind.BindManager

class Endpoint(components: {
  val system: ActorSystem
  val route: Route
}) {
  implicit val system = components.system

  import system.dispatcher

  def start(): Unit = {
    val host = System.getProperty(PropertyKey.PC_LISTEN_HOST, "0.0.0.0")
    val port = System.getProperty(PropertyKey.PC_LISTEN_PORT, "51600").toInt

    implicit val routingLog = new RoutingLog() {
      override def log: LoggingAdapter = system.log

      override def requestLog(request: HttpRequest): LoggingAdapter = log
    }
    implicit val materializer = ActorFlowMaterializer()
    implicit val settings = RoutingSettings.default(system)
    implicit val setup = RoutingSetup.apply
    val server = Http(components.system).bind(interface = host, port = port)
    server.to {
      Sink.foreach { connection =>
        connection.handleWithAsyncHandler(Route.asyncHandler(components.route))
      }
    }.run()
  }

}