package arimitsu.sf.packetconsole

sealed abstract class Protocol(val name: String)

object Protocol {

  case object TCP extends Protocol("tcp")

  case object UDP extends Protocol("udp")

  val list = List(TCP, UDP)

  def valueOf(proto: String): Protocol = {
    list.find(_.name == proto).get
  }
}
