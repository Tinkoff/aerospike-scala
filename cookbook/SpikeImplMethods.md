# SpikeImpl with methods

Create an instance of `SpikeImpl`
```scala
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")
```
`Put, Append, Prepend, Add`
```scala
import ru.tinkoff.aerospike.dsl.CallKB._
import com.aerospike.client.{AerospikeException, Key}
import com.aerospike.client.listener.{ExistsArrayListener, ExistsSequenceListener}
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class ExistsArrayHandler(keys: Array[Key] = Array(new Key("kName", "ns", 1)), exists: Array[Boolean] =
Array(true)) extends ExistsArrayListener {
  def onSuccess(keys: Array[Key], exists: Array[Boolean]): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}
case class ExistsSequenceHandler(k: Key = new Key("kName", "ns", 1), exists: Boolean = true) extends ExistsSequenceListener {
  def onExists(key: Key, exists: Boolean): Unit = {}
  def onSuccess(): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callKB(Put, "StrKey", SingleBin("binName", "binValue"))
spike.callKB(Put, "StrKey", MBin(Map("n1"-> "v1", "n2" -> "v2")))
spike.callKB(Append, "StrKey", SingleBin("binName", "binValue"))
spike.callKB(Append, "StrKey", MBin(Map("n1"-> "v1", "n2" -> "v2")))
spike.callKB(Prepend, "StrKey", SingleBin("binName", "binValue"))
spike.callKB(Prepend, "StrKey", MBin(Map("n1"-> "v1", "n2" -> "v2")))
spike.callKB(Add, "StrKey", SingleBin("binName", "binValue"))
spike.callKB(Add, "StrKey", MBin(Map("n1"-> "v1", "n2" -> "v2")))
```
`Execute`
```scala
import com.aerospike.client.query.Statement
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospike.dsl.Call.Execute
import ru.tinkoff.aerospike.dsl.Param1
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(Execute, Param1("zzz","zzz", List(...), Some(new Statement), None))
```
`Query`
 ```scala
import ru.tinkoff.aerospike.dsl.Call.Query
import ru.tinkoff.aerospike.dsl.Param2
import com.aerospike.client.query.Statement
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(Query, Param2(new Statement, None)) //or Some(com.aerospike.client.listener.RecordSequenceListener)
 ```
`QueryAggregate`
 ```scala
import ru.tinkoff.aerospike.dsl.Call.QueryAggregate
import ru.tinkoff.aerospike.dsl.Param1
import com.aerospike.client.query.Statement
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(QueryAggregate, Param1("zzz","zzz", List(), Some(new Statement), None))
spike.call(QueryAggregate, new Statement)
 ```
`ScanAll`
 ```scala
import ru.tinkoff.aerospike.dsl.Call.ScanAll
import ru.tinkoff.aerospike.dsl.{Param3, Param4}
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(ScanAll, Param3("namespace", "setName", List(), None)) // or Some(com.aerospike.client.ScanCallback)
spike.call(ScanAll, Param4("zzz","zzz", List(), None)) // or Some(com.aerospike.client.listener.RecordSequenceListener)
 ```
`RemoveUdf`
```scala
import ru.tinkoff.aerospike.dsl.Call.RemoveUdf
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(RemoveUdf, "serverPath")
```
`RegisterUdfString`
```scala
import ru.tinkoff.aerospike.dsl.Call.RegisterUdfString
import com.aerospike.client.Language
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospike.dsl.Param5
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

spike.call(RegisterUdfString, Param5("code", "serverPath", new Language()))
```
`Operate`
```scala
import ru.tinkoff.aerospike.dsl.CallK
import com.aerospike.client.Operation
import com.aerospike.client.Operation.Type
import com.aerospike.client.Value.StringValue
import com.aerospike.client.listener.RecordListener
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import com.aerospike.client.{AerospikeException, Key, Record}
import scala.collection.JavaConversions._
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospikemacro.converters.KeyWrapper.create
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

val stKeyWrapper = create[String]("ns", "setName")
val record1 = new Record(m1, 100, 12)

case class ReadHandler(key: Key = new Key("kName", "ns", 1),
                       record: Record = new Record(Map("k" -> new StringValue("v")), 100, 12)) extends RecordListener {
  def onSuccess(key: Key, record: Record): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

val ops = List(new Operation(Type.WRITE, "operateBinName", new StringValue("operate")), Operation.get("operateBinName"))

spike.callK(CallK.Operate, "strOperateKey", ops)
spike.callK(CallK.Operate, "strOperateKey", any = (ops, ReadHandler(stKeyWrapper("strOperateKey"), record1)))
```
`Delete`
```scala
import ru.tinkoff.aerospike.dsl.CallK.Delete
import com.aerospike.client.{AerospikeException, Key}
import com.aerospike.client.listener.DeleteListener
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class DeleteHandler(key: Key = new Key("kName", "ns", 1)) extends DeleteListener {
  def onSuccess(key: Key, existed: Boolean): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callK(Delete, "strDeleteKey", DeleteHandler())
spike.callK(Delete, "strDeleteKey")
```
`Touch`
```scala
import ru.tinkoff.aerospike.dsl.CallK.Touch
import com.aerospike.client.{AerospikeException, Key}
import com.aerospike.client.listener.WriteListener
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class WriteHandler(k: Key = new Key("kName", "ns", 1)) extends WriteListener {
  def onSuccess(key: Key): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callK(Touch, "strTouchKey", WriteHandler())
spike.callK(Touch, 3)
```
`Execute`
```scala
import ru.tinkoff.aerospike.dsl.CallK.Execute
import com.aerospike.client.{AerospikeException, Key}
import com.aerospike.client.listener.ExecuteListener
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import com.aerospike.client.Value.StringValue
import ru.tinkoff.aerospike.dsl.Param1
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class ExecuteHandler(k: Key = new Key("kName", "ns", 1), obj: Object = "") extends ExecuteListener {
  def onSuccess(key: Key, obj: Object): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callK(Execute, "strExecKey", Param1("pkg", "fName", List(new StringValue("str")), None, Option(ExecuteHandler())))
spike.callK(Execute, "strExecKey", Param1("pkg", "fName", List(new StringValue("str"))))
```
`Exists`
```scala
import ru.tinkoff.aerospike.dsl.CallK.Exists
import com.aerospike.client.{AerospikeException, Key}
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import com.aerospike.client.listener.ExistsListener
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class ExistsHandler(k: Key = new Key("kName", "ns", 1), exists: Boolean = true) extends ExistsListener {
  def onSuccess(key: Key, exists: Boolean): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callK(Exists, "strKey", ExistsHandler())
spike.callK(Exists, "strKey")
```
# Array[Key]
`Exists`
```scala
import ru.tinkoff.aerospike.dsl.CallKs.Exists
import com.aerospike.client.{AerospikeException, Key}
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikemacro.converters._
import com.aerospike.client.listener.{ExistsArrayListener, ExistsSequenceListener}
import scala.concurrent.ExecutionContext.Implicits.global

val client = AClient.client
val spike = new SpikeImpl(client)
implicit val dbc = DBCredentials("ns", "setName")

case class ExistsArrayHandler(keys: Array[Key] = Array(new Key("kName", "ns", 1)), exists: Array[Boolean] =
Array(true)) extends ExistsArrayListener {
  def onSuccess(keys: Array[Key], exists: Array[Boolean]): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}
case class ExistsSequenceHandler(k: Key = new Key("kName", "ns", 1), exists: Boolean = true) extends ExistsSequenceListener {
  def onExists(key: Key, exists: Boolean): Unit = {}
  def onSuccess(): Unit = {}
  def onFailure(e: AerospikeException): Unit = e.printStackTrace()
}

spike.callKs(Exists, Array("sk1", "sk2"), ExistsArrayHandler())
spike.callKs(Exists, Array("sk1", "sk2"), ExistsSequenceHandler())
spike.callKs(Exists, Array("sk1", "sk2"))
```
