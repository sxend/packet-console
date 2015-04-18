package arimitsu.sf.packetconsole.data

import java.net.InetSocketAddress

case class Bind(protocol: String, from: InetSocketAddress, to: InetSocketAddress)
