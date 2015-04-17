package arimitsu.sf.packetconsole

import akka.actor.ActorSystem
import akka.http.server.Route
import arimitsu.sf.packetconsole.api.{Endpoint, EndpointRoute}

trait Components {
  implicit val system: ActorSystem = ActorSystem("packet-console")
  val route: Route = EndpointRoute(this)
  val endpoint: Endpoint = new Endpoint(this)
}
