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

package ru.tinkoff.aerospikeexamples.example

import ru.tinkoff.aerospikemacro.printer.Printer
import ru.tinkoff.aerospikescala.domain.{ByteSegment, SingleBin}
import shapeless._

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.language.experimental.macros


/**
  * @author MarinaSigaeva 
  * @since 20.10.16
  */
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
  myObj.putArrayString("arrayStKey", SingleBin("arrayStBin", Array("abcd", "efgh", "ijkl")))
  myObj.putArrayInt("arrayIntKey", SingleBin("arrayInt", Array(3,6,8)))
  myObj.putArrayLong("arrayLongKey", SingleBin("arrayLong", Array(1L, 56L, 98L)))
  myObj.putArrayFloat("arrayFloatKey", SingleBin("arrayFloat", Array(1.12F, 2.13F, 3.5F)))
  myObj.putArrayDouble("arrayDoubleKey", SingleBin("arrayDouble", Array(12.13, 23.5, 46.5)))
  myObj.putArrayByte("arrayByteKey", SingleBin("arrayByteBin", Array(Byte.MinValue, Byte.MaxValue, Byte.MinValue)))
 // myObj.putByteSegment("byteSegmKey", SingleBin("byteSegment", ByteSegment(Array(Byte.MinValue, Byte.MaxValue), 12, 33)))

  myObj.putSample("sampleKey", SingleBin("sampleBin", Sample("sampleName", 2)))
  myObj.putHList("hListKey", SingleBin("hListBin", "hlist" :: 2 :: 3 :: HNil))
  myObj.putHList2("hListKey2", SingleBin("hListBin2", "hlist" :: 2 :: 3.12F :: List("a", "b") :: List(12, 23) :: HNil))
  myObj.putTuple("tuple3Key", SingleBin("tuple3Bin", ("abc", 2L, 3.12)))

  val mapSimpleString = Await.result(myObj.getMapSimpleString("mapSimpleString"), Inf)
  Printer.printNameValue(mapSimpleString)

  val mapStringString = Await.result(myObj.getMap("mapKey"), Inf)
  Printer.printNameValue(mapStringString)

  val mapIntString = Await.result(myObj.getMapIS("mapIntString"), Inf)
  Printer.printNameValue(mapIntString)

  val mapStringInt = Await.result(myObj.getMapSI("mapStringInt"), Inf)
  Printer.printNameValue(mapStringInt)

  val mapLong = Await.result(myObj.getMapLong("mapLong"), Inf)
  Printer.printNameValue(mapLong)

  val mapFloat = Await.result(myObj.getMapFloat("mapFloat"), Inf)
  Printer.printNameValue(mapFloat)

  val mapDouble = Await.result(myObj.getMapLong("mapDouble"), Inf)
  Printer.printNameValue(mapDouble)

  val strings = Await.result(myObj.getString("stringKey"), Inf)
  Printer.printNameValue(strings)

  val ints = Await.result(myObj.getInt("intBinKey"), Inf)
  Printer.printNameValue(ints)

  val floats = Await.result(myObj.getFloat("floatBinKey"), Inf)
  Printer.printNameValue(floats)

  val doubles = Await.result(myObj.getDouble("doubleBinKey"), Inf)
  Printer.printNameValue(doubles)

  val booleans = Await.result(myObj.getBoolean("boolBinKey"), Inf)
  Printer.printNameValue(booleans)

  val shorts = Await.result(myObj.getShort("shortBinKey"), Inf)
  Printer.printNameValue(shorts)

  val longs = Await.result(myObj.getLong("longBinKey"), Inf)
  Printer.printNameValue(longs)

  val chars = Await.result(myObj.getChar("charBinKey"), Inf)
  Printer.printNameValue(chars)

  val bytes = Await.result(myObj.getByte("byteBinKey"), Inf)
  Printer.printNameValue(bytes)

  val listStrs = Await.result(myObj.getListSt("listStBinKey"), Inf)
  Printer.printNameValue(listStrs)

  val listInt = Await.result(myObj.getListInt("listIntKey"), Inf)
  Printer.printNameValue(listInt)

  val listLong = Await.result(myObj.getListLong("listLongKey"), Inf)
  Printer.printNameValue(listLong)

  val listFloat = Await.result(myObj.getListFloat("listFloatKey"), Inf)
  Printer.printNameValue(listFloat)

  val listDouble = Await.result(myObj.getListDouble("listDoubleKey"), Inf)
  Printer.printNameValue(listDouble)

  val sample = Await.result(myObj.getSample("sampleKey"), Inf)
  Printer.printNameValue(sample)

  val hlist = Await.result(myObj.getHList("hListKey"), Inf)
  Printer.printNameValue(hlist)

  val hlist2 = Await.result(myObj.getHList2("hListKey2"), Inf)
  Printer.printNameValue(hlist2)

  val arrayString = Await.result(myObj.getArrayString("arrayStKey"), Inf)
  Printer.printNameValue(arrayString)

  val arrayInt = Await.result(myObj.getArrayInt("arrayIntKey"), Inf)
  Printer.printNameValue(arrayInt)

  val arrayLong = Await.result(myObj.getArrayLong("arrayLongKey"), Inf)
  Printer.printNameValue(arrayLong)

  val arrayFloat = Await.result(myObj.getArrayFloat("arrayFloatKey"), Inf)
  Printer.printNameValue(arrayFloat)

  val arrayDouble = Await.result(myObj.getArrayDouble("arrayDoubleKey"), Inf)
  Printer.printNameValue(arrayDouble)
/*
  val arrayByteBin = Await.result(myObj.getArrayByte("arrayByteKey"), Inf)
  Printer.printNameValue(arrayByteBin)*/

/*  val byteSegment = Await.result(myObj.getArrayByte("byteSegmKey"), Inf)
  Printer.printNameValue(byteSegment)*/

  /*  val tuple3 = Await.result(myObj.getTuple("tuple3Key"), Inf)
    Printer.printNameValue(tuple3)*/
}