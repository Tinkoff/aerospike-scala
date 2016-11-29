# aerospike-scala-dsl
![N|Solid](https://avatars0.githubusercontent.com/u/5486989?v=3&s=100)

To start working with Aerospike using this DSL you have to add dependency sbt:
```sh
"ru.tinkoff" % "aerospike-scala" % "1.1.10",
"com.aerospike" % "aerospike-client" % "3.3.0", // in case you don't have it
"ru.tinkoff" % "aerospike-scala-example" % "1.1.10" // usage examples
`````
Since I'm using Aerospike Java Client (version 3.3.0, recomended on www.aerospike.com),
you need to create com.aerospike.client.async.AsyncClient to pass it into `ru.tinkoff.aerospike.dsl.SpikeImpl` class.
`SpikeImpl` has methods to operate with Aerospike for specified types of `Keys` and `Bins`, which is the most common case.
Example for that object creation you can find in `ru.tinkoff.aerospikeexamples.example.AClient` (```sh "ru.tinkoff" % "aerospike-scala-example"```). 
Or just follow the `quickstart` instructions.

# Quickstart

Add this settings to application.conf file and specify your own host, port, namespace and setName:

```sh
ru-tinkoff-aerospike-dsl {
    keyWrapper-namespace = "test"
    keyWrapper-setName = "test"
    example-host = "somehost.com" 
    example-port = 3000
}
```
after that call `ru.tinkoff.aerospikeexamples.example.AClient.client` in your service (or something where you need to communicate with Aerospike)
and pass result of that call into `ru.tinkoff.aerospike.dsl.SpikeImpl`:
```scala
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
```
or you can call `AClient.spikeImpl`, which gives an example of `SpikeImpl` with host and port from `ru-tinkoff-aerospike-dsl` settings
```scala
 val spike = AClient.spikeImpl
```
**Note: don't forget to add host and port in application.conf, or you will get an exception from com.aerospike.client lib:**
```sh
Exception in thread "main" com.aerospike.client.AerospikeException$Connection: Error Code 11: Failed to connect to host(s): 
host 3000 Error Code 11: Invalid host: host 3000
```
For namespace and setName parameters add
```scala
implicit val dbc = AClient.dbc
```
Now you can use it like this:
```scala
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikescala.domain.SingleBin
import ru.tinkoff.aerospikemacro.converters._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object HelloAerospike extends App {

  val client = AClient.client
  val spike = new SpikeImpl(client)
  implicit val dbc = AClient.dbc

  Await.result(spike.callKB(CallKB.Put, "testKey", SingleBin("helloName", "helloValue")), Duration.Inf)
}
```
This will `Put` string value "helloValue" with name "helloName" in Aerospike with `Key` "testKey". For that call will be generated
converters, both to convert passed values into inner `com.aerospike.clients` values.
```js
aql> select * from test.test
[
  {
    "helloName": "helloValue"
  }
]

```
All available methods you can see in `SpikeImpl` class by your self. For more information and usage examples [cookbook](./cookbook).

# DSL schema options

In `ru.tinkoff.aerospike.dsl.scheme` we have two traits:
- for work with one key type and different types of `Bins`

```scala
trait Scheme[K]
```

- for work with one key type and one `Bin` type

 ```scala
trait KBScheme[K, B]
```

If you want to work with one type of Key and different types of Bins - [any Bin types](./cookbook/schemes/anyBinTypes.md)
If you want to work with one type of Key and one type of Bins [one Bin type](./cookbook/schemes/oneBinType.md)

# Application
Implemented for ASyncClient.
Recommended to use with `Aerospike 3`.