# One Key and Any-Bin-types schema

Let's try to use `Scheme[String]` where `Key` is `String` and `Bin` can be `any type` you like.
```scala
import com.aerospike.client.Value.MapValue
import ru.tinkoff.aerospike.dsl.errors.AerospikeDSLError
import ru.tinkoff.aerospike.dsl.scheme.Scheme
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospikescala.domain.SingleBin
import shapeless._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros


case class Sample(name: String, i: Int)

case class SampleScheme(spike: SpikeImpl) extends Scheme[String] {
 implicit val dbc = AClient.dbc

  implicit val sampleWrap = new BinWrapper[Sample] {
    override def fetch(any: Any): Option[Sample] = any match {
      case m: java.util.HashMap[Any, Any] => scala.util.Try(Sample(m.asScala("name").toString, m.asScala("i").toString.toInt)).toOption
      case _ => None
    }
  }

  implicit val sampleMapWrap = new BinWrapper[Map[Sample, String]] {
    val rex = "Sample\\((\\w+)\\,(\\d+)\\)"
    val trRex = rex.r

    override def toValue(v: Map[Sample, String]): MapValue =
      new MapValue(v.map { case (sample, value) => sample.toString -> value }.asJava)

    override def fetch(any: Any): Option[Map[Sample, String]] = any match {
      case m: java.util.HashMap[Any, String] => scala.util.Try(m.asScala.view.map {
        case (tr, v) if tr.toString.matches("Sample\\((\\w+)\\,(\\d+)\\)") =>
          tr.toString match {
            case trRex(n, i) => Sample(n, i.toInt) -> v
          }
      }.toMap).toOption
      case _ => None
    }
  }

  // Here is how to put basic types (you can use it as example and do the same for different kind of operations like Prepend, Add and so on)
  def putString(k: String, a: SingleBin[String])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, String](CallKB.Put, k, a)

  def putInt(k: String, a: SingleBin[Int])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Int](CallKB.Put, k, a)

  def putFloat(k: String, a: SingleBin[Float])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Float](CallKB.Put, k, a)

  def putDouble(k: String, a: SingleBin[Double])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Double](CallKB.Put, k, a)

  def putBoolean(k: String, a: SingleBin[Boolean])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Boolean](CallKB.Put, k, a)

  def putShort(k: String, a: SingleBin[Short])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Short](CallKB.Put, k, a)

  def putLong(k: String, a: SingleBin[Long])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Long](CallKB.Put, k, a)

  def putChar(k: String, a: SingleBin[Char])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Char](CallKB.Put, k, a)

  def putByte(k: String, a: SingleBin[Byte])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Byte](CallKB.Put, k, a)

  def putMap(k: String, a: SingleBin[Map[String, String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, String]](CallKB.Put, k, a)

  def putMapSimpleString(k: String, a: SingleBin[Map[Sample, String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[Sample, String]](CallKB.Put, k, a)

  def putMapIS(k: String, a: SingleBin[Map[Int, String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[Int, String]](CallKB.Put, k, a)

  def putMapSI(k: String, a: SingleBin[Map[String, Int]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Int]](CallKB.Put, k, a)

  def putMapLong(k: String, a: SingleBin[Map[String, Long]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Long]](CallKB.Put, k, a)

  def putMapFloat(k: String, a: SingleBin[Map[String, Float]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Float]](CallKB.Put, k, a)

  def putMapDouble(k: String, a: SingleBin[Map[String, Double]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Double]](CallKB.Put, k, a)

  def putHList(k: String, a: SingleBin[String :: Int :: Int :: HNil])(implicit e: ExecutionContext): Future[Unit] =
  spike.callKB[String, String :: Int :: Int :: HNil](CallKB.Put, k, a)

  def putHList2(k: String, a: SingleBin[String :: Int :: Float :: List[String] :: List[Int] :: HNil])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, String :: Int :: Float :: List[String] :: List[Int] :: HNil](CallKB.Put, k, a)

  def putListSt(k: String, a: SingleBin[List[String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[String]](CallKB.Put, k, a)

  def putListInt(k: String, a: SingleBin[List[Int]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Int]](CallKB.Put, k, a)

  def putListLong(k: String, a: SingleBin[List[Long]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Long]](CallKB.Put, k, a)

  def putListFloat(k: String, a: SingleBin[List[Float]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Float]](CallKB.Put, k, a)

  def putListDouble(k: String, a: SingleBin[List[Double]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Double]](CallKB.Put, k, a)

  def putSample(k: String, a: SingleBin[Sample])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Sample](CallKB.Put, k, a)

  def putTuple(k: String, a: SingleBin[(String, Long, Double)])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, (String, Long, Double)](CallKB.Put, k, a)

  def getString(k: String)(implicit e: ExecutionContext): Future[String] = spike.getByKey[String, String](k, Nil).map(_._1).map(_.values.head.getOrElse("no cat found"))

  def getInt(k: String)(implicit e: ExecutionContext): Future[Int] = spike.getByKey[String, Int](k, Nil).map(_._1).map(_.values.head.getOrElse(0))

  def getFloat(k: String)(implicit e: ExecutionContext): Future[Float] = spike.getByKey[String, Float](k, Nil).map(_._1).map(_.values.head.getOrElse(0F))

  def getDouble(k: String)(implicit e: ExecutionContext): Future[Double] = spike.getByKey[String, Double](k, Nil).map(_._1).map(_.values.head.getOrElse(0))

  def getBoolean(k: String)(implicit e: ExecutionContext): Future[Boolean] = spike.getByKey[String, Boolean](k, Nil).map(_._1).map(_.values.head.getOrElse(false))

  def getShort(k: String)(implicit e: ExecutionContext): Future[Short] = spike.getByKey[String, Short](k, Nil).map(_._1).map(_.values.head.getOrElse(0))

  def getLong(k: String)(implicit e: ExecutionContext): Future[Long] = spike.getByKey[String, Long](k, Nil).map(_._1).map(_.values.head.getOrElse(0L))

  def getChar(k: String)(implicit e: ExecutionContext): Future[Char] = spike.getByKey[String, Char](k, Nil).map(_._1).map(_.values.head.getOrElse('a'))

  def getByte(k: String)(implicit e: ExecutionContext): Future[Byte] = spike.getByKey[String, Byte](k, Nil).map(_._1).map(_.values.head.getOrElse(Byte.MaxValue))

  def getHList(k: String)(implicit e: ExecutionContext): Future[String :: Int :: Int :: HNil] = spike
    .getByKey[String, String :: Int :: Int :: HNil](k, Nil).map(_._1).map(_.values.head.get)

  def getHList2(k: String)(implicit e: ExecutionContext): Future[String :: Int :: Float :: List[String] :: List[Int] :: HNil] = spike
    .getByKey[String, String :: Int :: Float :: List[String] :: List[Int] :: HNil](k, Nil).map(_._1).map(_.values.head.get)

  def getSample(k: String)(implicit e: ExecutionContext): Future[Sample] = spike
    .getByKey[String, Sample](k, Nil).map(_._1).map(_.values.head.getOrElse(throw AerospikeDSLError("Failed to get Sample value from Aerospike")))

  def getTuple(k: String)(implicit e: ExecutionContext): Future[(String, Long, Double)] = spike
    .getByKey[String, (String, Long, Double)](k, Nil).map(_._1).map(_.values.head.getOrElse(throw AerospikeDSLError("Failed to get Sample value from Aerospike")))

  def getListSt(k: String)(implicit e: ExecutionContext): Future[List[String]] = spike.getByKey[String, List[String]](k, Nil).map(_._1).map(_.values.head.getOrElse(Nil))

  def getListInt(k: String)(implicit e: ExecutionContext): Future[List[Int]] = spike.getByKey[String, List[Int]](k, Nil).map(_._1).map(_.values.head.getOrElse(Nil))

  def getListLong(k: String)(implicit e: ExecutionContext): Future[List[Long]] = spike.getByKey[String, List[Long]](k, Nil).map(_._1).map(_.values.head.getOrElse(Nil))

  def getListFloat(k: String)(implicit e: ExecutionContext): Future[List[Float]] = spike.getByKey[String, List[Float]](k, Nil).map(_._1).map(_.values.head.getOrElse(Nil))

  def getListDouble(k: String)(implicit e: ExecutionContext): Future[List[Double]] = spike.getByKey[String, List[Double]](k, Nil).map(_._1).map(_.values.head.getOrElse(Nil))

  def getMap(k: String)(implicit e: ExecutionContext): Future[Map[String, String]] = spike.getByKey[String, Map[String, String]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapIS(k: String)(implicit e: ExecutionContext): Future[Map[Int, String]] = spike.getByKey[String, Map[Int, String]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapSI(k: String)(implicit e: ExecutionContext): Future[Map[String, Int]] = spike.getByKey[String, Map[String, Int]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapLong(k: String)(implicit e: ExecutionContext): Future[Map[String, Long]] = spike.getByKey[String, Map[String, Long]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapFloat(k: String)(implicit e: ExecutionContext): Future[Map[String, Float]] = spike.getByKey[String, Map[String, Float]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapDouble(k: String)(implicit e: ExecutionContext): Future[Map[String, Double]] = spike.getByKey[String, Map[String, Double]](k, Nil).map(_._1.values.head.getOrElse(Map()))

  def getMapSimpleString(k: String)(implicit e: ExecutionContext): Future[Map[Sample, String]] = spike
    .getByKey[String, Map[Sample, String]](k, Nil)(kC = kw, bC = sampleMapWrap, None, e).map(_._1.values.head.getOrElse(Map()))

}
```
`implicit val sampleWrap` -  This is custom wrapper for Bin of type Simple. By default all case classes stored in Aerospike as `Map[String, Any]`, where keys are names of parameters. 
For example 
```scala
Simple(name = "sampleName", i = 2)
```
will be stored as `Map("name" -> "sampleName", "i" -> 2)`. Inside `com.aerospike.client.Record` it will look like this:
```sh
bins:(sampleBin:{name=sampleName, i=2})
```
To get your `Sample` value properly you need to write fetch() function as it is shown above.
`implicit val sampleMapWrap` - This is custom wrapper for Bin of type `Map[Sample, String]`. For example for: 
```scala
Map(Sample(t1,3) -> v1, Sample(t2,2) -> v2, Sample(t3,1) -> v3)
```
`Bin` will be stored like `com.aerospike.client.Value.MapValue`, where keys are `Sample(...).toString()`.
So inside `com.aerospike.client.Record` it will look like this:
```sh
bins:(BIN_NAME:{Sample(t1,3)=v1, Sample(t2,2)=v2, Sample(t3,1)=v3})
```
And if you want to get your `Sample` type keys to be unwrapped properly you need to write fetch() function as it is shown above.
  
**Note**, Aerospikes `AQL` is good for values with `String` key types. So if you want to store `Map` with key of any other type - 
you will see nothing in terminal. But you can use function, which gets that value by key (for example `getMapIS(...)` above) and 
print it to be sure it all works fine. An example for `Map[Int, String]` is in `ru.tinkoff.aerospikeexamples.example.SampleApp.scala`:
```scala
 myObj.putMapIS("mapIntString", SingleBin("mapISName", Map(9 -> "v1", 2 -> "v2", 3 -> "v3")))
```
`HList` is stored as `Map[String, Any]` in Aerospike's `MapValue`. For example 
```scala "hlist" :: 2 :: 3 :: HNil``` will be stored as `Map("0" -> "hlist", "1" -> 2, "2" -> 3)`.
                                                        
Now we can use that scheme in App:
```scala
import ru.tinkoff.aerospikemacro.printer.Printer
import ru.tinkoff.aerospikescala.domain.SingleBin
import shapeless._

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.language.experimental.macros


object SampleApp extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  val spike = AClient.spikeImpl

  val myObj = SampleScheme(spike)

  myObj.putMapSimpleString("mapSimpleString", SingleBin("TmapBinName", Map(Sample("t1", 3) -> "v1", Sample("t2", 2) -> "v2", Sample("t3", 1) -> "v3")))
  myObj.putMap("mapKey", SingleBin("mapBinName", Map("a" -> "v1", "b" -> "v2", "c" -> "v3")))
  myObj.putMapIS("mapIntString", SingleBin("mapISName", Map(9 -> "v1", 2 -> "v2", 3 -> "v3")))
  myObj.putMapSI("mapStringInt", SingleBin("mapSIName", Map("a" -> 33, "b" -> 44, "c" -> 99)))
  myObj.putMapLong("mapLong", SingleBin("mapLongName", Map("a" -> 30030L, "b" -> 40004L, "c" -> 90009L)))
  myObj.putMapFloat("mapFloat", SingleBin("mapFloatName", Map("a" -> 30.3F, "b" -> 400.04F, "c" -> 9.01F)))
  myObj.putMapDouble("mapDouble", SingleBin("mapDoubleName", Map("a" -> 300.30, "b" -> 4000.4, "c" -> 90.09)))

  myObj.putString("stringKey", SingleBin("stringBinName", "strBinValue"))
  myObj.putInt("intBinKey", SingleBin("intBinName", 202))
  myObj.putFloat("floatBinKey", SingleBin("floatBinName", 1.11F))
  myObj.putDouble("doubleBinKey", SingleBin("doubleBinName", 3.3))
  myObj.putBoolean("boolBinKey", SingleBin("boolBinName", true))
  myObj.putShort("shortBinKey", SingleBin("shortBinName", 2))
  myObj.putLong("longBinKey", SingleBin("longBinName", 9000900L))
  myObj.putChar("charBinKey", SingleBin("charBinName", 'h'))
  myObj.putByte("byteBinKey", SingleBin("byteBinName", Byte.MinValue))

  myObj.putListSt("listStBinKey", SingleBin("listStringBin", List("a", "b")))
  myObj.putListInt("listIntKey", SingleBin("listIntBin", List(1, 2, 3, 4)))
  myObj.putListLong("listLongKey", SingleBin("listLongBin", List(1000L, 2000L, 3000L, 4000L)))
  myObj.putListFloat("listFloatKey", SingleBin("listFloatBin", List(1.12F, 2.13F, 3.5F, 4.5F)))
  myObj.putListDouble("listDoubleKey", SingleBin("listDoubleBin", List(12.11, 12.13, 23.5, 46.5)))

  myObj.putSample("sampleKey", SingleBin("sampleBin", Sample("sampleName", 2)))
  myObj.putHList("hListKey", SingleBin("hListBin", "hlist" :: 2 :: 3 :: HNil))
  myObj.putHList2("hListKey2", SingleBin("hListBin2", "hlist" :: 2 :: 3.12F :: List("a", "b") :: List(12, 23) :: HNil))
}
```
...and now we can see in Aerospike:
```js
aql> select * from test.test33
[
  {
    "TmapBinName": {} // explanation is below
  },                 
  {
    "intBinName": 202
  },
  {
    "longBinName": 9000900
  },
  {
    "shortBinName": 2
  },
  {
    "boolBinName": 1
  },
  {
    "listStringBin": [
      "a",
      "b"
    ]
  },
  {
    "charBinName": "h"
  },
  {
    "mapLongName": {
      "a": 30030,
      "b": 40004,
      "c": 90009
    }
  },
  {
    "listFloatBin": [
      1.1200000047683716,
      2.130000114440918,
      3.5,
      4.5
    ]
  },
  {
    "mapBinName": {
      "a": "v1",
      "b": "v2",
      "c": "v3"
    }
  },
  {
    "byteBinName": -128
  },
  {
    "doubleBinName": 3.2999999999999998
  },
  {
    "mapISName": {}
  },
  {
    "listLongBin": [
      1000,
      2000,
      3000,
      4000
    ]
  },
  {
    "listIntBin": [
      1,
      2,
      3,
      4
    ]
  },
  {
    "listDoubleBin": [
      12.109999999999999,
      12.130000000000001,
      23.5,
      46.5
    ]
  },
  {
    "hListBin2": {
      "0": "hlist",
      "1": 2,
      "2": 3.119999885559082,
      "3": "AC ED 00 05 73 72 00 32 73 63 61 6C 61 2E 63 6F 6C 6C 65 63 74 69 6F 6E 2E 69 6D 6D 75 74 61 62 6C 65 2E 4C 69 73 74 24 53 65 72 69 61 6C 69 7A 61 74 69 6F 6E 50 72 6F 78 79 00 00 00 00 00 00 00 01 03 00 00 78 70 74 00 01 61 74 00 01 62 73 72 00 2C 73 63 61 6C 61 2E 63 6F 6C 6C 65 63 74 69 6F 6E 2E 69 6D 6D 75 74 61 62 6C 65 2E 4C 69 73 74 53 65 72 69 61 6C 69 7A 65 45 6E 64 24 8A 5C 63 5B F7 53 0B 6D 02 00 00 78 70 78",
      "4": "AC ED 00 05 73 72 00 32 73 63 61 6C 61 2E 63 6F 6C 6C 65 63 74 69 6F 6E 2E 69 6D 6D 75 74 61 62 6C 65 2E 4C 69 73 74 24 53 65 72 69 61 6C 69 7A 61 74 69 6F 6E 50 72 6F 78 79 00 00 00 00 00 00 00 01 03 00 00 78 70 73 72 00 11 6A 61 76 61 2E 6C 61 6E 67 2E 49 6E 74 65 67 65 72 12 E2 A0 A4 F7 81 87 38 02 00 01 49 00 05 76 61 6C 75 65 78 72 00 10 6A 61 76 61 2E 6C 61 6E 67 2E 4E 75 6D 62 65 72 86 AC 95 1D 0B 94 E0 8B 02 00 00 78 70 00 00 00 0C 73 71 00 7E 00 02 00 00 00 17 73 72 00 2C 73 63 61 6C 61 2E 63 6F 6C 6C 65 63 74 69 6F 6E 2E 69 6D 6D 75 74 61 62 6C 65 2E 4C 69 73 74 53 65 72 69 61 6C 69 7A 65 45 6E 64 24 8A 5C 63 5B F7 53 0B 6D 02 00 00 78 70 78"
    }
  },
  {
    "hListBin": {
      "0": "hlist",
      "1": 2,
      "2": 3
    }
  },
  {
    "stringBinName": "strBinValue"
  },
  {
    "mapFloatName": {
      "a": 30.299999237060547,
      "b": 400.04000854492188,
      "c": 9.0100002288818359
    }
  },
  {
    "sampleBin": {
      "i": "2",
      "name": "sampleName"
    }
  },
  {
    "mapDoubleName": {
      "a": 300.30000000000001,
      "b": 4000.4000000000001,
      "c": 90.090000000000003
    }
  },
  {
    "floatBinName": 1.1100000143051147
  },
  {
    "mapSIName": {
      "a": 33,
      "b": 44,
      "c": 99
    }
  }
]
```
`AQL` has an issue, it can't show us complicated keys stored, but using current DSL we can `getMapSimpleString(...)` and look: 
```scala
//"TmapBinName": {}

mapSimpleString => Map(Sample(t1,3) -> v1, Sample(t2,2) -> v2, Sample(t3,1) -> v3)
```
all information is here!

To clean up all data stored during testing with apps made above, write some cleaner app and add there all keys.
For example:
```scala
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikemacro.converters.KeyWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import scala.language.experimental.macros


object CleanUp extends App {

  val client = AClient.client
  val spike = new SpikeImpl(client)
  implicit val dbc = AClient.dbc

  val keys = List("mapKey", "mapSimpleString", "mapStringString", "mapIntString", "mapStringInt", "mapLong", "mapFloat", "mapDouble", "stringKey",
    "intBinKey", "floatBinKey", "doubleBinKey", "boolBinKey", "shortBinKey", "longBinKey", "charBinKey", "byteBinKey",
    "listStBinKey", "listIntKey", "listLongKey", "listFloatKey", "listDoubleKey", "sampleKey", "hListKey", "oneKey", "manyKey", "hListKey2", "tuple3Key")

  val result = for (key <- keys) yield spike.deleteK(key)
  Await.result(Future.sequence(result), Inf)

}
```
Now set is empty:
```js
aql> select * from test.test33
[
]
```