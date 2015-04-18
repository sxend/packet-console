package arimitsu.sf.packetconsole.data

import java.net.InetSocketAddress

case class Node(host: String, port: Int) {
  def toInet: InetSocketAddress = new InetSocketAddress(host, port)
}
