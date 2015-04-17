packet-console
==============

packet-console is application level packet repeater  

packet repeating feature based on akka-IO, and web administration tools

## start
java -Dpacket.console.port=51600 -jar packet-console.jar

## feature

* TCP/UDP port forwarding
* configuration, statistics API

## API

* GET /api/binds
```
[
    {
        "id": "<uuid:String>",
        "protocol": "(tcp|udp)",
        "port" : "<number: Int>"
    }
]
```
* GET /api/binds/{id}
```
{
    "id": "<uuid:String>",
    "protocol": "(tcp|udp)",
    "port" : "<number: Int>"
}
```
* PUT /api/binds/{protocol}/{from_host}:{from_port}/{to_host}:{to_port}

* DELETE /api/binds/{id}
* GET /api/protocols
-- GET /api/statistics
-- POST /api/shutdown

