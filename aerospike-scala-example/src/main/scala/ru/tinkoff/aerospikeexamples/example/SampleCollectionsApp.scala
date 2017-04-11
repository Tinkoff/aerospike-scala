package ru.tinkoff.aerospikeexamples.example

import ru.tinkoff.aerospikemacro.printer.Printer
import ru.tinkoff.aerospikescala.domain.{ByteSegment, SingleBin}
import shapeless._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.language.experimental.macros

/**
  * @author MarinaSigaeva
  * @since 04.04.17
  */
object SampleCollectionsApp extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  val spike = AClient.spikeImpl

  val myObj = SampleScheme(spike)

  myObj.putListSt("listStBinKey", SingleBin("listStringBin", List("a", "b")))
  myObj.putListInt("listIntKey", SingleBin("listIntBin", List(1, 2, 3, 4)))
  myObj.putListLong("listLongKey", SingleBin("listLongBin", List(1000L, 2000L, 3000L, 4000L)))
  myObj.putListFloat("listFloatKey", SingleBin("listFloatBin", List(1.12F, 2.13F, 3.5F, 4.5F)))
  myObj.putListDouble("listDoubleKey", SingleBin("listDoubleBin", List(12.11, 12.13, 23.5, 46.5)))
  myObj.putListBoolean("listBoolKey", SingleBin("listBoolBin", List(true, false, false, true)))
  myObj.putArrayString("arrayStKey", SingleBin("arrayStBin", Array("abcd", "efgh", "ijkl")))
  myObj.putArrayInt("arrayIntKey", SingleBin("arrayInt", Array(3, 6, 8)))
  myObj.putArrayLong("arrayLongKey", SingleBin("arrayLong", Array(1L, 56L, 98L)))
  myObj.putArrayFloat("arrayFloatKey", SingleBin("arrayFloat", Array(1.12F, 2.13F, 3.5F)))
  myObj.putArrayDouble("arrayDoubleKey", SingleBin("arrayDouble", Array(12.13, 23.5, 46.5)))
  myObj.putArrayByte("arrayByteKey", SingleBin("arrayByteBin", Array(Byte.MinValue, Byte.MaxValue, Byte.MinValue)))
  myObj.putArrayBoolean("arrayBoolKey", SingleBin("arrayBoolBin", Array(true, false, true)))
  myObj.putSeqArrayBuffer("seqArrBuff", SingleBin("ww", Seq(ArrayBuffer(1.2, 3.1, 5.6))))
  // myObj.putByteSegment("byteSegmKey", SingleBin("byteSegment", ByteSegment(Array(Byte.MinValue, Byte.MaxValue), 12, 33)))

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

  val listBoolean = Await.result(myObj.getListBoolean("listBoolKey"), Inf)
  Printer.printNameValue(listBoolean)

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

  val arrayBoolean = Await.result(myObj.getArrayBoolean("arrayBoolKey"), Inf)
  Printer.printNameValue(arrayBoolean)

  val arrayByteBin = Await.result(myObj.getArrayByte("arrayByteKey"), Inf)
  Printer.printNameValue(arrayByteBin)

  /*  val seqArrBuff = Await.result(myObj.getSeqArrayBuffer("seqArrBuff"), Inf)
  Printer.printNameValue(seqArrBuff)*/
}
