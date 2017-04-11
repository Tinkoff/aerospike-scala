/*
 * Copyright (c) 2016 Tinkoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.aerospikemacro.converters

import java.util.{List => JList, Map => JMap}

import com.aerospike.client.{Bin, Record}
import com.aerospike.client.Value.{ValueArray, _}
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import spray.json._

import scala.collection.JavaConverters._
import scala.collection.mutable.{Seq => mSeq}
import scala.language.experimental.macros
import shapeless.{HList, _}
import shapeless.HList.hlistOps
import syntax.std.traversable._
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}
import ru.tinkoff.aerospikemacro.cast.Tuplify._

/**
  * @author MarinaSigaeva
  * @since 08.09.16
  */
//noinspection TypeAnnotation
class BinTest extends FlatSpec with Matchers with OptionValues {

  type R = (Int, String)

  val mh: JMap[String, Any] = Map("0" -> 2.0, "1" -> List("a", "b"), "2" -> 2, "3" -> "dsdsds").asJava

  "getBin" should "test BinWrapper with default ones" in new mocks {
    getBin(SingleBin("name", stringValue)) shouldBe stringBin
    getBin(SingleBin("name", shortValue)) shouldBe shortBin
    getBin(SingleBin("name", byteValue)) shouldBe byteBin
    getBin(SingleBin("name", intValue)) shouldBe intBin
    getBin(SingleBin("name", longValue)) shouldBe longBin
    getBin(SingleBin("name", floatValue)) shouldBe floatBin
    getBin(SingleBin("name", doubleValue)) shouldBe doubleBin
    getBin(SingleBin("name", booleanValue)) shouldBe booleanBin
    getBin(SingleBin("name", List(1, 2, 3))) shouldBe intListBin
    getBin(SingleBin("name", Seq(1, 2, 3))) shouldBe intSeqBin
    getBin(SingleBin("name", List(1, 2, 3))).value.toString shouldBe "[1, 2, 3]"
    getBin(SingleBin("name", Array[Byte](192.toByte, 168.toByte, 1, 9))) shouldBe arrayByteBin
    getBin(SingleBin("name", Map("k1" -> 2, "k2" -> 3))) shouldBe intMapBin
    getBin(SingleBin("name", Map("k1" -> 2, "k2" -> List(1, 2, 3)))) shouldBe anyMapBin
    getBin(SingleBin("name", Map(1 -> List(1, 2, 3).asJava).asJava)) shouldBe mapBin
    getBin(SingleBin("name", Map(1 -> List(1, 2, 3)))).value.getObject.toString shouldBe """{1=List(1, 2, 3)}"""
    getBin(SingleBin("name", hList)).value.getObject.toString shouldBe """{0=2.0, 1=List(a, b), 2=2, 3=dsdsds}"""
    getBin(SingleBin("name", hList)).value.getObject shouldBe mh
    getBin(SingleBin("name", hList)) shouldBe mapBinH
    getBin(SingleBin("name", (2.toDouble, List("a", "b"), 2, "dsdsds"))) shouldBe mapBinH
    getBin(SingleBin("name", Array("a", "b", "c"))) shouldBe arrayStringBin

  }

  it should "test BinWrapper with custom ones" in new mocks {

    implicit val intM = new BinWrapper[Map[Int, List[Int]]] {
      override def toValue(m: Map[Int, List[Int]]): MapValue = {
        val value = m.mapValues {
          case ls: List[_] => ls.mkString(",")
          case oth         => oth.toString()
        }
        new MapValue(value.asJava)
      }
    }

    getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc1).value.getObject.toString shouldBe
      """{"mega-name":"truck","mega-number":4,"mega-color":[1,2,3]}"""

    getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc2).value.toString shouldBe
      """{name=truck, number=4, color=List(1, 2, 3)}"""

    getBinL(SingleBin("name", List(Truck("truck1", 14, List(10, 22, 33)), Truck("truck2", 33, List(1, 2, 3)))))(tc2).value
      .isInstanceOf[ValueArray] shouldBe true

    getBinL(SingleBin("name", List(Truck("truck1", 14, List(10, 22, 33)), Truck("truck2", 33, List(1, 2, 3)))))(tc1).value shouldBe tsBlobArray

    getBin(SingleBin("name", Map(1 -> List(1, 2, 3)))).value.getObject.toString shouldBe """{1=1,2,3}"""
  }

  it should "test BinWrapper for making case class from Aerospike Record" in new fromInsideToBtMocks {
    truckMap.values.head.value shouldBe truck
    gen shouldBe 100
    exp shouldBe 12
  }

  it should "throw ClassCastException because fromInsideToBt in BinWrapperer is not implemented" in new fromInsideToBtMocks {
    val duck                           = Duck("duck", List(1, 2, 3))
    val duckValue                      = getBin[Duck](SingleBin("duckNameBin", duck)).value
    val jduckMap: JMap[String, Object] = Map[String, AnyRef]("duck1" -> duckValue).asJava
    val duckRecord                     = new Record(jduckMap, 100, 12)

    def getDuckName(r: Record)(implicit bC: BinWrapper[Duck]): Option[String] = bC(r)._1.values.head.map(_.name)

    intercept[ClassCastException](getDuckName(duckRecord)).getMessage shouldBe
      "com.aerospike.client.Value$MapValue cannot be cast to ru.tinkoff.aerospikemacro.converters.Duck"
  }

  "BinWrapper" should "correctly extract values from aerospike Record" in new RecordMock {
    stringRecord.singleValue.value shouldBe stringValue
    shortRecord.singleValue.value shouldBe shortValue
    byteRecord.singleValue.value shouldBe byteValue
    intRecord.singleValue.value shouldBe intValue
    longRecord.singleValue.value shouldBe longValue
    floatRecord.singleValue.value shouldBe floatValue
    doubleRecord.singleValue.value shouldBe doubleValue
    booleanRecord.singleValue.value shouldBe booleanValue
  }

}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val truckFormat: RootJsonFormat[Truck] = jsonFormat(Truck, "mega-name", "mega-number", "mega-color")
}

case class Truck(name: String, number: Int, color: List[Int])

case class Duck(name: String, color: List[Int])

//noinspection TypeAnnotation
trait mocks {
  def getBins[B](b: MBin[B])(implicit bC: BinWrapper[B]): List[Bin] = bC.apply(b)

  def getBin[B](b: SingleBin[B])(implicit bC: BinWrapper[B]): Bin = bC.apply(b)

  def getBinL[B](b: SingleBin[List[B]])(implicit bC: BinWrapper[B]): Bin = {
    val listValue = b.value.view.map(lb => bC.apply(SingleBin(b.name, lb))).map(_.value).toArray
    new Bin(b.name, new ValueArray(listValue))
  }

  implicit val tc1 = new BinWrapper[Truck] {

    import MyJsonProtocol._

    override def toValue(truck: Truck): BlobValue = {
      val j = truck.toJson(truckFormat)
      new BlobValue(j)
    }
  }

  implicit val tc2 = new BinWrapper[Truck] {
    override def fetch(any: Any): Option[Truck] = {
      any match {
        case mv: MapValue =>
          mv.getObject match {
            case m: java.util.Map[String, Any] =>
              val nm = m.asScala.toMap
              Option(Truck(nm("name").toString, nm("number").toString.toInt, nm("color").asInstanceOf[List[Int]]))
          }
        case _ => None
      }
    }
  }

  val hList             = 2.toDouble :: List("a", "b") :: 2 :: "dsdsds" :: HNil
  val stringValue       = "strValue"
  val shortValue        = 3.toShort
  val byteValue         = 3.toByte
  val intValue          = 3
  val longValue         = 3L
  val floatValue        = 3F
  val doubleValue       = 3D
  val booleanValue      = true
  val stringBin         = new Bin("name", stringValue)
  val shortBin          = new Bin("name", shortValue)
  val byteBin           = new Bin("name", byteValue)
  val intBin            = new Bin("name", intValue)
  val longBin           = new Bin("name", longValue)
  val floatBin          = new Bin("name", floatValue)
  val doubleBin         = new Bin("name", doubleValue)
  val booleanBin        = new Bin("name", booleanValue)
  val jl: JList[Int]    = List(1, 2, 3).asJava
  val jlh: List[String] = List("a", "b")

  val trMap1: JMap[String, Any]        = Map("name" -> "truck1", "number" -> 14, "colors" -> List(10, 22, 33)).asJava
  val trMap2: JMap[String, Any]        = Map("name" -> "truck2", "number" -> 33, "colors" -> List(1, 2, 3)).asJava
  val trList: JList[JMap[String, Any]] = List(trMap1, trMap2).asJava
  val trListBinValue                   = new ListValue(List(trMap1, trMap2).asJava)

  val t1          = new MapValue(Map("name" -> "truck1", "number" -> "14", "color" -> List(10, 22, 33)).asJava)
  val t2          = new MapValue(Map("name" -> "truck2", "number" -> "33", "color" -> List(1, 2, 3)).asJava)
  val twoTruckBin = new Bin("name", new ValueArray(Array(t1, t2)))

  import MyJsonProtocol._

  val t02     = Truck("truck1", 14, List(10, 22, 33))
  val jt02    = t02.toJson(truckFormat)
  val tsBlob1 = new BlobValue(jt02)
  val t03     = Truck("truck2", 33, List(1, 2, 3))
  val jt03    = t03.toJson(truckFormat)
  val tsBlob3 = new BlobValue(jt03)

  val tsBlobArray = new ValueArray(Array(tsBlob1, tsBlob3))

  val js: JList[Int]                 = Seq(1, 2, 3).asJava
  val jls: JList[String]             = List("a", "b", "c").asJava
  val intListBin                     = new Bin("name", jl)
  val intSeqBin                      = new Bin("name", js)
  val arrayByteBin                   = new Bin("name", Array[Byte](192.toByte, 168.toByte, 1, 9))
  val arrayStringBin                 = new Bin("name", jls)
  val truckBin                       = new Bin("name", Truck("truck", 4, List(1, 2, 3)))
  val mSeqBin                        = new Bin("name", mSeq(1, 2, 3))
  val mapList: JMap[Int, JList[Int]] = Map(1 -> jl).asJava
  val mapListH: JMap[String, Any]    = Map("0" -> 2.0, "1" -> jlh, "2" -> 2, "3" -> "dsdsds").asJava
  val mapBin                         = new Bin("name", mapList)
  val mapBinH                        = new Bin("name", mapListH)
  val intMap: JMap[String, Int]      = Map("k1" -> 2, "k2" -> 3).asJava
  val anyMap: JMap[String, Any]      = Map("k1" -> 2, "k2" -> List(1, 2, 3)).asJava
  val intMapBin                      = new Bin("name", intMap)
  val anyMapBin                      = new Bin("name", anyMap)

}

//noinspection TypeAnnotation
trait fromInsideToBtMocks extends mocks {
  val truck                      = Truck("truck", 4, List(1, 2, 3))
  val truckValue                 = getBin(SingleBin("name", truck))(tc2).value
  val jMap: JMap[String, AnyRef] = Map[String, AnyRef]("truck1" -> truckValue).asJava
  val record                     = new Record(jMap, 100, 12)
  val (truckMap, gen, exp)       = tc2(record)
}

trait RecordMock extends mocks {

  class TestRecord[T](val record: Record) {
    def valueMap(implicit bw: BinWrapper[T]): Map[String, Option[T]] = bw(record)._1
    def singleValue(implicit bw: BinWrapper[T]): Option[T]           = valueMap.head._2
  }

  implicit class RecordsOps[T](value: T) {

    def toSingleTestRecord(binName: String = "name"): TestRecord[T] = {
      def f(value: AnyRef) =
        new TestRecord[T](new Record(Map[String, AnyRef](binName -> value).asJava, 1, 1))

      value match {
        case v: Double  => f(new java.lang.Double(v))
        case v: Float   => f(new java.lang.Double(v))
        case v: Long    => f(new java.lang.Long(v))
        case v: Byte    => f(new java.lang.Long(v))
        case v: Short   => f(new java.lang.Long(v))
        case v: Int     => f(new java.lang.Long(v))
        case v: Boolean => f(new java.lang.Long(if (v) 1 else 0))
        case v: String  => f(v)
      }
    }
  }

  val stringRecord: TestRecord[String]   = stringValue.toSingleTestRecord()
  val shortRecord: TestRecord[Short]     = shortValue.toSingleTestRecord()
  val byteRecord: TestRecord[Byte]       = byteValue.toSingleTestRecord()
  val intRecord: TestRecord[Int]         = intValue.toSingleTestRecord()
  val longRecord: TestRecord[Long]       = longValue.toSingleTestRecord()
  val floatRecord: TestRecord[Float]     = floatValue.toSingleTestRecord()
  val doubleRecord: TestRecord[Double]   = doubleValue.toSingleTestRecord()
  val booleanRecord: TestRecord[Boolean] = booleanValue.toSingleTestRecord()
}


