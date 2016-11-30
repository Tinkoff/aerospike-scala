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

import com.aerospike.client.{Bin, Record, Value}
import com.aerospike.client.Value.{ValueArray, _}
import org.scalatest.{FlatSpec, Matchers}
import spray.json.{DefaultJsonProtocol, _}

import collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.{Seq => mSeq}
import scala.language.experimental.macros
import shapeless.{HList, _}
import shapeless.HList.hlistOps
import syntax.std.traversable._
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.collection.immutable.ListMap


/**
  * @author MarinaSigaeva 
  * @since 08.09.16
  */
class BinTest extends FlatSpec with Matchers {

  type R = (Int, String)

  val mh: JMap[String, Any] = Map("0" -> 2.0, "1" -> List("a", "b"), "2" -> 2, "3" -> "dsdsds")

  "getBin" should "test BinWrapper with default ones" in new mocks {
    getBin(SingleBin("name", "strValue")) shouldBe stringBin
    getBin(SingleBin("name", 3)) shouldBe intBin
    getBin(SingleBin("name", List(1, 2, 3))) shouldBe intListBin
    getBin(SingleBin("name", Seq(1, 2, 3))) shouldBe intSeqBin
    getBin(SingleBin("name", List(1, 2, 3))).value.toString shouldBe "[1, 2, 3]"
    getBin(SingleBin("name", Array[Byte](192.toByte, 168.toByte, 1, 9))) shouldBe arrayByteBin
    getBin(SingleBin("name", Map("k1" -> 2, "k2" -> 3))) shouldBe intMapBin
    getBin(SingleBin("name", Map("k1" -> 2, "k2" -> List(1, 2, 3)))) shouldBe anyMapBin
    getBin(SingleBin("name", mapAsJavaMap(Map(1 -> seqAsJavaList(List(1, 2, 3)))))) shouldBe mapBin
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
          case ls: List[_] => ls.mkString(comma)
          case oth => oth.toString()
        }
        new MapValue(value)
      }
    }

    getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc1).value.getObject.toString shouldBe
      """{"mega-name":"truck","mega-number":4,"mega-color":[1,2,3]}"""

    getBin(SingleBin("name", Truck("truck", 4, List(1, 2, 3))))(tc2).value.toString shouldBe
      """{name=truck, number=4, color=List(1, 2, 3)}"""

    getBinL(SingleBin("name", List(Truck("truck1", 14, List(10, 22, 33)), Truck("truck2", 33, List(1, 2, 3)))))(tc2).value.isInstanceOf[ValueArray] shouldBe true

    getBinL(SingleBin("name", List(Truck("truck1", 14, List(10, 22, 33)), Truck("truck2", 33, List(1, 2, 3)))))(tc1).value shouldBe tsBlobArray

    getBin(SingleBin("name", Map(1 -> List(1, 2, 3)))).value.getObject.toString shouldBe """{1=1,2,3}"""
  }

  it should "test BinWrapper for making case class from Aerospike Record" in new fromInsideToBtMocks {
    truckMap.values.head shouldBe Some(truck)
    gen shouldBe 100
    exp shouldBe 12
  }

  /*  it should "test BinWrapper for getting simple type from aerospike Record" in new fromInsideToBtSimple {

      val (st, gen, exp) = getFromRecord(record2)
      st shouldBe Map("name", Some("str"))
      gen shouldBe 100
      exp shouldBe 12

      val (ls, gen2, exp2) = getFromRecord[List[String]](record3)//(BinWrapper.listS)
      ls shouldBe Map("name", Some(List("str", "str")))
      gen2 shouldBe 100
      exp2 shouldBe 12

      val (lsi, geni, expi) = getFromRecord[List[Int]](record8)//(BinWrapper.listS)
      lsi shouldBe Map("name", Some(List(8, 9, 7)))
      geni shouldBe 100
      expi shouldBe 12

      val (m, gen3, exp3) = getFromRecord[Map[String, String]](record4)
      m shouldBe Map("name", Some(Map("strKey1" -> "strValue1", "strKey2" -> "strValue2")))
      gen3 shouldBe 100
      exp3 shouldBe 12

      val (m2, gen4, exp4) = getFromRecord[Map[String, Int]](record5)
      m2 shouldBe Map("name", Some(Map("k1" -> 4, "k2" -> 5)))
      gen4 shouldBe 100
      exp4 shouldBe 12

      val (m3, gen5, exp5) = getFromRecord[Map[String, List[Int]]](record6)
      m3 shouldBe Map("name", Some(Map("k1" -> List(1, 2, 3), "k2" -> List(4, 5, 6))))
      gen5 shouldBe 100
      exp5 shouldBe 12

      val (m4, gen6, exp6) = getFromRecord[Map[String, Any]](record7)
      m4 shouldBe Map("name", Some(Map("k1" -> List(1, 2, 3), "k2" -> 33)))
      gen6 shouldBe 100
      exp6 shouldBe 12

      val (sth, genh, exph) = getFromRecord[Int :: String :: Double :: List[Int] :: List[String]::HNil](recordh)
      sth shouldBe Map("name", Some(1 :: "aff" :: 39.0 :: List(1, 2) :: List("a", "z") :: HNil))
      genh shouldBe 100
      exph shouldBe 12

      val (stt2, gent2, expt2) = getFromRecord[(Int, String)](recordTuple2)
      stt2 shouldBe Map("name", Some((1, "aff")))

      val (stt3, gent3, expt3) = getFromRecord[(Int, String, Double)](recordTuple3)
      stt3 shouldBe Map("name", Some((1, "aff", 39.0)))

      val (stt4, gent4, expt4) = getFromRecord[(Int, String, Double, List[Int])](recordTuple4)
      stt4 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2))))

      val (stt5, gent5, expt5) = getFromRecord[(Int, String, Double, List[Int], List[String])](recordTuple5)
      stt5 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"))))
      gent5 shouldBe 100
      expt5 shouldBe 12

      val (stt6, gent6, expt6) = getFromRecord[(Int, String, Double, List[Int], List[String], String)](recordTuple6)
      stt6 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), "aff")))

      val (stt7, gent7, expt7) = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String)](recordTuple7)
      stt7 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff")))

      val (stt8, gent8, expt8) = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String, Double)](recordTuple8)
      stt8 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0)))

      val (stt9, gent9, expt9) = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String, Double, List[Int])](recordTuple9)
      stt9 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1,2))))

      val (stt10, gent10, expt10) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String])](recordTuple10)
      stt10 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1,2), List("a", "z"))))

      val (stt11, gent11, expt11) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int)](recordTuple11)
      stt11 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1,2), List("a", "z"), 22)))

      val (stt12, gent12, expt12) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String)](recordTuple12)
      stt12 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www")))

      val (stt13, gent13, expt13) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int)](recordTuple13)
      stt13 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3)))

      val (stt14, gent14, expt14) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int])](recordTuple14)
      stt14 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5))))

      val (stt15, gent15, expt15) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String)](recordTuple15)
      stt15 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www")))

      val (stt16, gent16, expt16) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String, Int)](recordTuple16)
      stt16 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4)))

      val (stt17, gent17, expt17) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String, Int, String)](recordTuple17)
      stt17 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk")))

      val (stt18, gent18, expt18) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
        String, Int, String, Int)](recordTuple18)
      stt18 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8)))

      val (stt19, gent19, expt19) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
        String, Int, String, Int, String)](recordTuple19)
      stt19 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk")))

      val (stt20, gent20, expt20) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
        String, Int, String, Int, String, Double)](recordTuple20)
      stt20 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0)))

      val (stt21, gent21, expt21) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
        String, Int, String, Int, String, Double, Boolean)](recordTuple21)
      stt21 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0, true)))

      val (stt22, gent22, expt22) = getFromRecord[(Int, String, Double, List[Int], List[String],
        Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
        String, Int, String, Int, String, Double, Boolean, String)](recordTuple22)
      stt22 shouldBe Map("name", Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
        39.0, List(1,2), List("a", "z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0, true, "www")))

    }*/

  it should "throw ClassCastException because fromInsideToBt in BinWrapperer is not implemented" in new fromInsideToBtMocks {
    val duck = Duck("duck", List(1, 2, 3))
    val duckBin = getBin[Duck](SingleBin("duckNameBin", duck))
    val duckValue = duckBin.value
    val jduckMap: JMap[String, Object] = Map("duck1" -> duckValue)
    val duckRecord = new Record(jduckMap, 100, 12)

    def getDuckName(r: Record)(implicit bC: BinWrapper[Duck]): Option[String] = bC(r)._1.values.head.map(_.name)

    intercept[ClassCastException](getDuckName(duckRecord)).getMessage shouldBe
      "com.aerospike.client.Value$MapValue cannot be cast to ru.tinkoff.aerospikemacro.converters.Duck"
  }

}

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val truckFormat = jsonFormat(Truck, "mega-name", "mega-number", "mega-color")
}

case class Truck(name: String, number: Int, color: List[Int])

case class Duck(name: String, color: List[Int])


trait mocks {
  def getBins[B](b: MBin[B])(implicit bC: BinWrapper[B]): List[Bin] = bC.apply(b)

  def getBin[B](b: SingleBin[B])(implicit bC: BinWrapper[B]): Bin = bC.apply(b)

  def getBinL[B](b: SingleBin[List[B]])(implicit bC: BinWrapper[B]): Bin = {
    val listValue = b.value.view.map(lb => bC.apply(SingleBin(b.name, lb))).map(_.value).toArray
    new Bin(b.name, new ValueArray(listValue))
  }

  def getFromRecord[B](r: Record)(implicit bC: BinWrapper[B]): (Map[String, Any], Int, Int) = bC.apply(r)

  implicit val tc1 = new BinWrapper[Truck] {

    import MyJsonProtocol._

    override def toValue(truck: Truck) = {
      val j = truck.toJson(truckFormat)
      new BlobValue(j)
    }
  }

  implicit val tc2 = new BinWrapper[Truck] {
    override def fetch(any: Any): Option[Truck] = {
      any match {
        case mv: MapValue => mv.getObject match {
          case m: java.util.Map[String, Any] => val nm = m.asScala.toMap
            Option(Truck(nm("name").toString, nm("number").toString.toInt,
              nm("color").asInstanceOf[List[Int]]))
        }
        case other => None
      }
    }
  }

  val hList = 2.toDouble :: List("a", "b") :: 2 :: "dsdsds" :: HNil
  val stringBin = new Bin("name", "strValue")
  val intBin = new Bin("name", 3)
  val jl: JList[Int] = List(1, 2, 3)
  val jlh: List[String] = List("a", "b")

  val trMap1: JMap[String, Any] = Map("name" -> "truck1", "number" -> 14, "colors" -> List(10, 22, 33))
  val trMap2: JMap[String, Any] = Map("name" -> "truck2", "number" -> 33, "colors" -> List(1, 2, 3))
  val trList: JList[JMap[String, Any]] = List(trMap1, trMap2)
  val trListBinValue = new ListValue(List(trMap1, trMap2))

  val t1 = new MapValue(Map("name" -> "truck1", "number" -> "14", "color" -> List(10, 22, 33)))
  val t2 = new MapValue(Map("name" -> "truck2", "number" -> "33", "color" -> List(1, 2, 3)))
  val twoTruckBin = new Bin("name", new ValueArray(Array(t1, t2)))

  import MyJsonProtocol._

  val t02 = Truck("truck1", 14, List(10, 22, 33))
  val jt02 = t02.toJson(truckFormat)
  val tsBlob1 = new BlobValue(jt02)
  val t03 = Truck("truck2", 33, List(1, 2, 3))
  val jt03 = t03.toJson(truckFormat)
  val tsBlob3 = new BlobValue(jt03)

  val tsBlobArray = new ValueArray(Array(tsBlob1, tsBlob3))

  val js: JList[Int] = Seq(1, 2, 3)
  val jls: JList[String] = List("a", "b", "c")
  val intListBin = new Bin("name", jl)
  val intSeqBin = new Bin("name", js)
  val arrayByteBin = new Bin("name", Array[Byte](192.toByte, 168.toByte, 1, 9))
  val arrayStringBin = new Bin("name", jls)
  val truckBin = new Bin("name", Truck("truck", 4, List(1, 2, 3)))
  val mSeqBin = new Bin("name", mSeq(1, 2, 3))
  val mapList: JMap[Int, JList[Int]] = Map(1 -> jl)
  val mapListH: JMap[String, Any] = Map("0" -> 2.0, "1" -> jlh, "2" -> 2, "3" -> "dsdsds")
  val mapBin = new Bin("name", mapList)
  val mapBinH = new Bin("name", mapListH)
  val intMap: JMap[String, Int] = Map("k1" -> 2, "k2" -> 3)
  val anyMap: JMap[String, Any] = Map("k1" -> 2, "k2" -> List(1, 2, 3))
  val intMapBin = new Bin("name", intMap)
  val anyMapBin = new Bin("name", anyMap)

}

trait fromInsideToBtMocks extends mocks {
  val truck = Truck("truck", 4, List(1, 2, 3))
  val truckValue = getBin(SingleBin("name", truck))(tc2).value
  val jMap: JMap[String, Object] = Map("truck1" -> truckValue)
  val record = new Record(jMap, 100, 12)
  val (truckMap, gen, exp) = tc2(record)
}

/*
trait fromInsideToBtSimple extends mocks {
  val rValue = getBin(SingleBin("name", "str")).value
  val rMap: JMap[String, Object] = Map("name", rValue)
  val record2 = new Record(rMap, 100, 12)

  val rValue3 = getBin(SingleBin("name", List("str", "str"))).value
  val rMap3: JMap[String, Object] = Map("name", rValue3)
  val record3 = new Record(rMap3, 100, 12)

  val rValue8 = getBin(SingleBin("name", List(8,9,7))).value
  val rMap8: JMap[String, Object] = Map("name", rValue8)
  val record8 = new Record(rMap8, 100, 12)

  val rValue4 = getBin(SingleBin("name", Map("strKey1" -> "strValue1", "strKey2" -> "strValue2"))).value
  val rMap4: JMap[String, Object] = Map("name", rValue4)
  val record4 = new Record(rMap4, 100, 12)

  val rValue5 = getBin(SingleBin("name", Map("k1" -> 4L, "k2" -> 5L))).value
  val rMap5: JMap[String, Object] = Map("name", rValue5)
  val record5 = new Record(rMap5, 100, 12)

  val rValue6 = getBin(SingleBin("name", Map("k1" -> List(1,2,3), "k2" -> List(4,5,6)))).value
  val rMap6: JMap[String, Object] = Map("name", rValue6)
  val record6 = new Record(rMap6, 100, 12)

  val rValue7 = getBin(SingleBin("name", Map("k1" -> List(1,2,3), "k2" -> 33))).value
  val rMap7: JMap[String, Object] = Map("name", rValue7)
  val record7 = new Record(rMap7, 100, 12)

  val rhValue = getBin(SingleBin("name", (1 :: "aff" :: 39.0 :: List(1,2) :: List("a","z") :: HNil))).value
  val rhMap: JMap[String, Object] = Map("name", rhValue)
  val recordh = new Record(rhMap, 100, 12)

  val rtValue2 = getBin(SingleBin("name", (1, "aff"))).value
  val rtMap2: JMap[String, Object] = Map("name", rtValue2)
  val recordTuple2 = new Record(rtMap2, 100, 12)

 val rtValue3 = getBin(SingleBin("name", (1, "aff", 39.0))).value
  val rtMap3: JMap[String, Object] = Map("name", rtValue3)
  val recordTuple3 = new Record(rtMap3, 100, 12)

  val rtValue4 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2)))).value
  val rtMap4: JMap[String, Object] = Map("name", rtValue4)
  val recordTuple4 = new Record(rtMap4, 100, 12)

  val rtValue5 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z")))).value
  val rtMap5: JMap[String, Object] = Map("name", rtValue5)
  val recordTuple5 = new Record(rtMap5, 100, 12)

  val rtValue6 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"), "aff"))).value
  val rtMap6: JMap[String, Object] = Map("name", rtValue6)
  val recordTuple6 = new Record(rtMap6, 100, 12)

  val rtValue7 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"), 1, "aff"))).value
  val rtMap7: JMap[String, Object] = Map("name", rtValue7)
  val recordTuple7 = new Record(rtMap7, 100, 12)

   val rtValue8 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"), 1, "aff", 39.0))).value
   val rtMap8: JMap[String, Object] = Map("name", rtValue8)
   val recordTuple8 = new Record(rtMap8, 100, 12)

   val rtValue9 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"), 1, "aff", 39.0, List(1,2)))).value
   val rtMap9: JMap[String, Object] = Map("name", rtValue9)
   val recordTuple9 = new Record(rtMap9, 100, 12)

   val rtValue10 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
     1, "aff", 39.0, List(1,2), List("a","z")))).value
   val rtMap10: JMap[String, Object] = Map("name", rtValue10)
   val recordTuple10 = new Record(rtMap10, 100, 12)

   val rtValue11 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
     1, "aff", 39.0, List(1,2), List("a","z"), 22))).value
   val rtMap11: JMap[String, Object] = Map("name", rtValue11)
   val recordTuple11 = new Record(rtMap11, 100, 12)

   val rtValue12 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
     1, "aff", 39.0, List(1,2), List("a","z"), 22, "www"))).value
   val rtMap12: JMap[String, Object] = Map("name", rtValue12)
   val recordTuple12 = new Record(rtMap12, 100, 12)

  val rtValue13 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3))).value
  val rtMap13: JMap[String, Object] = Map("name", rtValue13)
  val recordTuple13 = new Record(rtMap13, 100, 12)

  val rtValue14 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5)))).value
  val rtMap14: JMap[String, Object] = Map("name", rtValue14)
  val recordTuple14 = new Record(rtMap14, 100, 12)

  val rtValue15 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www"))).value
  val rtMap15: JMap[String, Object] = Map("name", rtValue15)
  val recordTuple15 = new Record(rtMap15, 100, 12)

  val rtValue16 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4))).value
  val rtMap16: JMap[String, Object] = Map("name", rtValue16)
  val recordTuple16 = new Record(rtMap16, 100, 12)

  val rtValue17 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk"))).value
  val rtMap17: JMap[String, Object] = Map("name", rtValue17)
  val recordTuple17 = new Record(rtMap17, 100, 12)

  val rtValue18 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8))).value
  val rtMap18: JMap[String, Object] = Map("name", rtValue18)
  val recordTuple18 = new Record(rtMap18, 100, 12)

  val rtValue19 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk"))).value
  val rtMap19: JMap[String, Object] = Map("name", rtValue19)
  val recordTuple19 = new Record(rtMap19, 100, 12)

  val rtValue20 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0))).value
  val rtMap20: JMap[String, Object] = Map("name", rtValue20)
  val recordTuple20 = new Record(rtMap20, 100, 12)

  val rtValue21 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0, true))).value
  val rtMap21: JMap[String, Object] = Map("name", rtValue21)
  val recordTuple21 = new Record(rtMap21, 100, 12)

  val rtValue22 = getBin(SingleBin("name", (1, "aff", 39.0, List(1,2), List("a","z"),
    1, "aff", 39.0, List(1,2), List("a","z"), 22, "www", 3, List(4,5), "www", 4, "kkk", 8, "kkkk", 9.0, true, "www"))).value
  val rtMap22: JMap[String, Object] = Map("name", rtValue22)
  val recordTuple22 = new Record(rtMap22, 100, 12)

}
*/
