package arimitsu.sf.packetconsole.api.data

import java.net.InetSocketAddress

case class Bind(protocol: String, from: InetSocketAddress, to: InetSocketAddress)
