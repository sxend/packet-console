package arimitsu.sf.packetconsole.api

import akka.actor.ActorSystem
import akka.http._
import akka.http.server.Route
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink

class Endpoint(components: {
  val system: ActorSystem
  val route: Route
}) {
  def start(): Unit = {

    implicit val materializer = ActorFlowMaterializer()
    val host = System.getProperty("packet.console.host", "0.0.0.0")
    val port = System.getProperty("packet.console.port", "51600").toInt
    val server = Http(components.system).bind(interface = host, port = port)
    server.to {
      Sink.foreach { connection =>
        connection.handleWithAsyncHandler(Route.asyncHandler(components.route))
      }
    }.run()
  }

}