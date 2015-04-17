package arimitsu.sf.packetconsole

import akka.actor.ActorSystem

trait Components {
  implicit val system: ActorSystem
}
