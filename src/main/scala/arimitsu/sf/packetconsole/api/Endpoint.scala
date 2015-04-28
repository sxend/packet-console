package arimitsu.sf.packetconsole.api

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Server
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Sink._
import arimitsu.sf.packetconsole.{PacketConsoleException, PropertyKey}

class Endpoint(components: {
  val system: ActorSystem
  val requestMapping: RequestMapping
}) {
  implicit val system = components.system
  lazy val route = components.requestMapping.route

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
      foreach { connection =>
        connection.handleWithAsyncHandler(Route.asyncHandler {
          validate {
            route
          }
        })
      }
    }.run()
  }

  private def validate(r: => Route) = parameter('credential) {
    case c if c == credential => pathPrefix("api") {
      respondWithHeaders(Server("packet-console"))(r)
    }
    case any: Any =>
      system.log.info(s"invalid credential message: $any")
      reject
  }

  private lazy val credential = {
    Option(System.getProperty(PropertyKey.PC_API_CREDENTIAL)).getOrElse {
      throw new PacketConsoleException("credential property is required.")
    }
  }
}