package arimitsu.sf.packetconsole

import org.scalatest._

class BootstrapSpec extends FlatSpec with Matchers {
  "Bootstrap" should "exit" in {
    Bootstrap.main(Array[String]())
  }
}
