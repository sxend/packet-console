package arimitsu.sf.packetconsole

import java.net.InetSocketAddress

import arimitsu.sf.packetconsole.data.{Node, Bind}
import spray.json.DefaultJsonProtocol._
import spray.json._

package object api {

  object Protocol extends DefaultJsonProtocol{
    implicit val nodeFormat = jsonFormat2(Node.apply)
    implicit val bindFormat = jsonFormat4(Bind.apply)
  }

}
