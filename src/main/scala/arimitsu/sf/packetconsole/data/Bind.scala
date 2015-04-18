package arimitsu.sf.packetconsole.data

import arimitsu.sf.packetconsole.Protocol

case class Bind(id: String, protocol: Protocol, from: Node, to: Node)
