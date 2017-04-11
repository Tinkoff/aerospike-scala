package ru.tinkoff.aerospikemacro.converters

import java.util.{List => JList, Map => JMap}

import com.aerospike.client.Record
import com.aerospike.client.Value.{MapValue, StringValue}
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import ru.tinkoff.aerospikescala.domain.SingleBin
import shapeless.{::, HNil}

import scala.collection.JavaConverters._
import ru.tinkoff.aerospikemacro.cast.Caster._
import ru.tinkoff.aerospikemacro.cast.Tuplify._
import ru.tinkoff.aerospikemacro.cast.Tuplify


/**
  * @author MarinaSigaeva 
  * @since 11.04.17
  */
class GetFromRecordTest extends FlatSpec with Matchers with OptionValues {

  it should "test BinWrapper for getting simple type from aerospike Record" in new fromInsideToBtSimple {

    val st = getFromRecord[String](record2)
    st shouldBe Map("name" -> Some("str"))

    val ls = getFromRecord[List[String]](record3) //(BinWrapper.listS)
    ls shouldBe Map("name" -> Some(List("str", "str")))

    val lsi = getFromRecord[List[Int]](record8) //(BinWrapper.listS)
    lsi shouldBe Map("name" -> Some(List(8, 9, 7)))

    val m = getFromRecord[Map[String, String]](record4)
    m shouldBe Map("name" -> Some(Map("strKey1" -> "strValue1", "strKey2" -> "strValue2")))

    val m2 = getFromRecord[Map[String, Int]](record5)
    m2 shouldBe Map("name" -> Some(Map("k1" -> 4, "k2" -> 5)))

    val m3 = getFromRecord[Map[String, List[Int]]](record6)
    m3 shouldBe Map("name" -> Some(Map("k1" -> List(1, 2, 3), "k2" -> List(4, 5, 6))))

    val m4 = getFromRecord[Map[String, Any]](record7)
    m4 shouldBe Map("name" -> Some(Map("k1" -> List(1, 2, 3), "k2" -> 33)))

    val sth = getFromRecord[Int :: String :: Double :: List[Int] :: List[String] :: HNil](recordh)
    sth shouldBe Map("name" -> Some(1 :: "aff" :: 39.0 :: List(1, 2) :: List("a", "z") :: HNil))

    val stt2 = getFromRecord[(Int, String)](recordTuple2)
    stt2 shouldBe Map("name" -> Some((1, "aff")))

    val stt3 = getFromRecord[(Int, String, Double)](recordTuple3)
    stt3 shouldBe Map("name" -> Some((1, "aff2", 39.0)))

    val stt4 = getFromRecord[(Int, String, Double, List[Int])](recordTuple4)
    stt4 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2))))

    val stt5 = getFromRecord[(Int, String, Double, List[Int], List[String])](recordTuple5)
    stt5 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"))))

    val stt6 = getFromRecord[(Int, String, Double, List[Int], List[String], String)](recordTuple6)
    stt6 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), "aff")))

    val stt7 = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String)](recordTuple7)
    stt7 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff")))

    val stt8 = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String, Double)](recordTuple8)
    stt8 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0)))

    val stt9 = getFromRecord[(Int, String, Double, List[Int], List[String], Int, String, Double, List[Int])](recordTuple9)
    stt9 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1, 2))))

    val stt10 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String])](recordTuple10)
    stt10 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1, 2), List("a", "z"))))

    val stt11 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int)](recordTuple11)
    stt11 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1, 2), List("a", "z"), 22)))

    val stt12 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String)](recordTuple12)
    stt12 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www")))

    val stt13 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int)](recordTuple13)
    stt13 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3)))

    val stt14 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int])](recordTuple14)
    stt14 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5))))

    val stt15 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String)](recordTuple15)
    stt15 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www")))

    val stt16 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String, Int)](recordTuple16)
    stt16 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4)))

    val stt17 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int], String, Int, String)](recordTuple17)
    stt17 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk")))

    val stt18 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
      String, Int, String, Int)](recordTuple18)
    stt18 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8)))

    val stt19 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
      String, Int, String, Int, String)](recordTuple19)
    stt19 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk")))

    val stt20 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
      String, Int, String, Int, String, Double)](recordTuple20)
    stt20 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0)))

    val stt21 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
      String, Int, String, Int, String, Double, Boolean)](recordTuple21)
    stt21 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0, true)))

    val stt22 = getFromRecord[(Int, String, Double, List[Int], List[String],
      Int, String, Double, List[Int], List[String], Int, String, Int, List[Int],
      String, Int, String, Int, String, Double, Boolean, String)](recordTuple22)
    stt22 shouldBe Map("name" -> Some((1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff",
      39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0, true, "www")))
  }
}

trait fromInsideToBtSimple extends mocks {

  def getFromRecord[B](r: Record)(implicit bC: BinWrapper[B]): Map[String, Option[B]] = bC.apply(r)._1

  val rMap: JMap[String, Object] = Map[String, Object]("name" -> new StringValue("str")).asJava
  val record2 = new Record(rMap, 100, 12)

  val rValue3 = getBin(SingleBin("name", List("str", "str"))).value
  val rMap3: JMap[String, Object] = Map[String, Object]("name" -> rValue3).asJava
  val record3 = new Record(rMap3, 100, 12)

  val rValue8 = getBin(SingleBin("name", List(8, 9, 7))).value
  val rMap8: JMap[String, Object] = Map[String, Object]("name" -> rValue8).asJava
  val record8 = new Record(rMap8, 100, 12)

  val rValue4 = getBin(SingleBin("name", Map("strKey1" -> "strValue1", "strKey2" -> "strValue2"))).value
  val rMap4: JMap[String, Object] = Map[String, Object]("name" -> rValue4).asJava
  val record4 = new Record(rMap4, 100, 12)

  val rValue5 = getBin(SingleBin("name", Map("k1" -> 4L, "k2" -> 5L))).value
  val rMap5: JMap[String, Object] = Map[String, Object]("name" -> rValue5).asJava
  val record5 = new Record(rMap5, 100, 12)

  val rValue6 = getBin(SingleBin("name", Map("k1" -> List(1, 2, 3), "k2" -> List(4, 5, 6)))).value
  val rMap6: JMap[String, Object] = Map[String, Object]("name" -> rValue6).asJava
  val record6 = new Record(rMap6, 100, 12)

  val rValue7 = getBin(SingleBin("name", Map("k1" -> List(1, 2, 3), "k2" -> 33))).value
  val rMap7: JMap[String, Object] = Map[String, Object]("name" -> rValue7).asJava
  val record7 = new Record(rMap7, 100, 12)

  val rhValue = new MapValue(Map(0 -> 1, 1 -> "aff", 2 -> 39.0, 3 -> List(1, 2), 4 -> List("a", "z")).asJava)
  val rhMap: JMap[String, Object] = Map[String, Object]("name" -> rhValue).asJava
  val recordh = new Record(rhMap, 100, 12)

  val rtValue2 = new MapValue(Map("0" -> 1L, "1" -> "aff").asJava)
  val rtMap2: JMap[String, Object] = Map[String, Object]("name" -> rtValue2).asJava
  val recordTuple2 = new Record(rtMap2, 100, 12)

  val rtValue3 = new MapValue(Map("0" -> 1, "1" -> "aff2", "2" -> 39.0).asJava)
  val rtMap3: JMap[String, Object] = Map[String, Object]("name" -> rtValue3).asJava
  val recordTuple3 = new Record(rtMap3, 100, 12)

  val rtValue4 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2)))).value
  val rtMap4: JMap[String, Object] = Map[String, Object]("name" -> rtValue4).asJava
  val recordTuple4 = new Record(rtMap4, 100, 12)

  val rtValue5 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z")))).value
  val rtMap5: JMap[String, Object] = Map[String, Object]("name" -> rtValue5).asJava
  val recordTuple5 = new Record(rtMap5, 100, 12)

  val rtValue6 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"), "aff"))).value
  val rtMap6: JMap[String, Object] = Map[String, Object]("name" -> rtValue6).asJava
  val recordTuple6 = new Record(rtMap6, 100, 12)

  val rtValue7 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff"))).value
  val rtMap7: JMap[String, Object] = Map[String, Object]("name" -> rtValue7).asJava
  val recordTuple7 = new Record(rtMap7, 100, 12)

  val rtValue8 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0))).value
  val rtMap8: JMap[String, Object] = Map[String, Object]("name" -> rtValue8).asJava
  val recordTuple8 = new Record(rtMap8, 100, 12)

  val rtValue9 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"), 1, "aff", 39.0, List(1, 2)))).value
  val rtMap9: JMap[String, Object] = Map[String, Object]("name" -> rtValue9).asJava
  val recordTuple9 = new Record(rtMap9, 100, 12)

  val rtValue10 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z")))).value
  val rtMap10: JMap[String, Object] = Map[String, Object]("name" -> rtValue10).asJava
  val recordTuple10 = new Record(rtMap10, 100, 12)

  val rtValue11 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22))).value
  val rtMap11: JMap[String, Object] = Map[String, Object]("name" -> rtValue11).asJava
  val recordTuple11 = new Record(rtMap11, 100, 12)

  val rtValue12 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www"))).value
  val rtMap12: JMap[String, Object] = Map[String, Object]("name" -> rtValue12).asJava
  val recordTuple12 = new Record(rtMap12, 100, 12)

  val rtValue13 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3))).value
  val rtMap13: JMap[String, Object] = Map[String, Object]("name" -> rtValue13).asJava
  val recordTuple13 = new Record(rtMap13, 100, 12)

  val rtValue14 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5)))).value
  val rtMap14: JMap[String, Object] = Map[String, Object]("name" -> rtValue14).asJava
  val recordTuple14 = new Record(rtMap14, 100, 12)

  val rtValue15 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www"))).value
  val rtMap15: JMap[String, Object] = Map[String, Object]("name" -> rtValue15).asJava
  val recordTuple15 = new Record(rtMap15, 100, 12)

  val rtValue16 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4))).value
  val rtMap16: JMap[String, Object] = Map[String, Object]("name" -> rtValue16).asJava
  val recordTuple16 = new Record(rtMap16, 100, 12)

  val rtValue17 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk"))).value
  val rtMap17: JMap[String, Object] = Map[String, Object]("name" -> rtValue17).asJava
  val recordTuple17 = new Record(rtMap17, 100, 12)

  val rtValue18 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8))).value
  val rtMap18: JMap[String, Object] = Map[String, Object]("name" -> rtValue18).asJava
  val recordTuple18 = new Record(rtMap18, 100, 12)

  val rtValue19 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk"))).value
  val rtMap19: JMap[String, Object] = Map[String, Object]("name" -> rtValue19).asJava
  val recordTuple19 = new Record(rtMap19, 100, 12)

  val rtValue20 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0))).value
  val rtMap20: JMap[String, Object] = Map[String, Object]("name" -> rtValue20).asJava
  val recordTuple20 = new Record(rtMap20, 100, 12)

  val rtValue21 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0, true))).value
  val rtMap21: JMap[String, Object] = Map[String, Object]("name" -> rtValue21).asJava
  val recordTuple21 = new Record(rtMap21, 100, 12)

  val rtValue22 = getBin(SingleBin("name", (1, "aff", 39.0, List(1, 2), List("a", "z"),
    1, "aff", 39.0, List(1, 2), List("a", "z"), 22, "www", 3, List(4, 5), "www", 4, "kkk", 8, "kkkk", 9.0, true, "www"))).value
  val rtMap22: JMap[String, Object] = Map[String, Object]("name" -> rtValue22).asJava
  val recordTuple22 = new Record(rtMap22, 100, 12)

}