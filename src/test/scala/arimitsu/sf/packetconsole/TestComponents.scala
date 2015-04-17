package arimitsu.sf.packetconsole

import akka.actor.ActorSystem

class TestComponents extends Components {
  override implicit val system: ActorSystem = ActorSystem("test-system")
}

object TestComponents extends TestComponents