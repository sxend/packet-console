package arimitsu.sf.packetconsole

object Bootstrap {
  def main(args: Array[String]): Unit = {
    val components = new Components {}
    new Bootstrap(components).run()
  }
}

class Bootstrap(components: Components) {
  def run(): Unit = {
    components.endpoint.start()
  }
}