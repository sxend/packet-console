package arimitsu.sf.packetconsole

import arimitsu.sf.packetconsole.Protocol._
import arimitsu.sf.packetconsole.data.{Bind, Node}
import spray.json._

package object api {

  object JsonFormats extends DefaultJsonProtocol {
    implicit val nodeFormat = jsonFormat2(Node.apply)
    implicit val protocolFormat = new RootJsonFormat[Protocol] {
      override def write(obj: Protocol): JsValue = {
        new JsObject(Map("protocol" -> new JsString(obj.name)))
      }

      override def read(json: JsValue): Protocol = {
        val jo = json.asJsObject.fields.get("protocol")
        jo match {
          case Some(proto) =>
            proto.toString() match {
              case TCP.name => TCP
              case UDP.name => UDP
              case any: Any => throw new PacketConsoleException(s"unknown protocol $any")
            }
          case any: Any => throw new PacketConsoleException(s"unsupported protocol name $any")
        }
      }
    }
    implicit val bindFormat = jsonFormat4(Bind.apply)
  }

}
