# BinWrapper

Types of created keys detected from passed value. I recommend to use `Int, Long, String, Boolean, Float, Double, Array[Byte], Seq, List or Map`. 
Also there is `ru.tinkoff.aerospikescala.domain.ByteSegment` case class which corresponds to `com.aerospike.client.ByteSegmentValue`. 
Converter for `Bin` supports a lot of types, including `HLists` and your own `case classes`.
For using some case class as a `Bin` you will have to show (```scala override def fetch(any: Any): Option[YourType] = { ... }``` ) how to store it in Aerospike.

Usage is very simple:
```scala
def getBin[B](b: SingleBin[B])(implicit bC: BinWrapper[B]): Bin = bC.apply(b)

getBin(SingleBin("name", 3)) shouldBe new Bin("name", 3)
getBin(SingleBin("name", List(1, 2, 3))) shouldBe new Bin("name", Seq(1, 2, 3))
```
`HLists` and `tuples` are stored as `Maps` (here are same result Maps):
```scala
getBin(SingleBin("name", 2.toDouble :: List("a","b") :: 2 :: "dsdsds" :: HNil)) shouldBe
   new Bin("name", Map(0 -> 2.0, 1 -> List[String] = List("a", "b"), 2 -> 2, 3 -> "dsdsds"))
getBin(SingleBin("name", (2.toDouble, List("a","b"), 2, "dsdsds"))) shouldBe
   new Bin("name", Map(0 -> 2.0, 1 -> List[String] = List("a", "b"), 2 -> 2, 3 -> "dsdsds"))
```
If you need to use `case class` as a value:
```scala
case class Truck(name: String, number: Int, color: List[Int])
```
Assume we have two different ideas for how to store and (which is more important) how to get our `Truck` value from Aerospike.
Option one - let it store as a `Map` (which is a default case, means no need to do anything else), but then I have to ```scala override fetch``` function like this:
```scala
  implicit val tc2 = new BinWrapper[Truck] {
    override def fetch(any: Any): Option[Truck] = {
      scala.util.Try{ any match {
        case m: java.util.Map[String, Any] => val nm = m.asScala.toMap
          Truck(nm("name").toString, nm("number").toString.toInt,
          nm("color").toString.split(",").view.map(_.toInt).toList)
      }}.toOption
    }
  }
```
Option two: store it like a `json`
```scala
  implicit val tc1 = new BinWrapper[Truck] {
    import MyJsonProtocol._

    override def toValue(truck: Truck) = {
      val j = truck.toJson(truckFormat)
      new BlobValue(j)
    }
  }

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val truckFormat = jsonFormat(Truck, "mega-name", "mega-number", "mega-color")
  }
```
usage:
```scala
getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc1) in Aerospike it will look like {"mega-name":"truck","mega-number":4,"mega-color":[1,2,3]}
getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc2) in Aerospike it will look like {name=truck, number=4, color=1,2,3}
```
`Wrappers` passed explicitly, because we have two of them in this scope.

**Note**: saving as `BlobValue, GeoJSONValue, ValueArray` or `NullValue` not implemented in `BinWrapper`. Your case classes will be saved as `Map[String, Any]` in `com.aerospike.client.MapValue<String, Object>`.
If you want another format just override  `toValue()` function in `BinWrapper` creation.