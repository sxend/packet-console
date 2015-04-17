package arimitsu.sf.packetconsole

import akka.actor.ActorSystem

object Bootstrap {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("packet-console")
    val components = new Components {
      override implicit val system: ActorSystem = system
    }
    new Bootstrap(components).run()
  }
}

class Bootstrap(components: Components) {
  import components.system.dispatcher
  def run(): Unit = {

  }
}