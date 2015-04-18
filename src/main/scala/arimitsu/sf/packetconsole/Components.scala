package arimitsu.sf.packetconsole

import akka.actor.ActorSystem
import akka.http.server.Route
import arimitsu.sf.packetconsole.api._
import arimitsu.sf.packetconsole.bind.BindManager

trait Components {
  implicit val system: ActorSystem = ActorSystem("packet-console")
  lazy val bindsHandler: BindsHandler = new BindsHandler(this)
  lazy val protocolsHandler: ProtocolsHandler = new ProtocolsHandler(this)
  lazy val statisticsHandler: StatisticsHandler = new StatisticsHandler(this)
  lazy val route: Route = new RequestMapping(this).route
  lazy val endpoint: Endpoint = new Endpoint(this)
  lazy val bindManager: BindManager = new BindManager(this)
}
