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

import com.aerospike.client.Value.MapValue
import ru.tinkoff.aerospike.dsl.errors.AerospikeDSLError
import ru.tinkoff.aerospike.dsl.scheme.Scheme
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospikescala.domain.{ByteSegment, SingleBin}
import shapeless._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.aerospike.client.Value

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros


/**
  * @author MarinaSigaeva
  * @since 26.09.16
  *
  *        That Scheme example demonstrates how to work with Key of type String and different Bin types
  */

case class Sample(name: String, i: Int)

case class SampleScheme(spike: SpikeImpl) extends Scheme[String] {
  implicit val dbc = AClient.dbc

  /* This is custom wrapper for Bin of type Simple. By default all case classes stored in Aerospike as Map[String, Any],
  where keys are names of parameters. For example Simple(name = "sampleName", i = 2) will be stored as Map("name" -> "sampleName", "i" -> 2)&
  Inside com.aerospike.client.Record it will look like this:
  bins:(sampleBin:{name=sampleName, i=2})
  To get your Sample value properly you need to write fetch() function as it is shown below
  */
  implicit val sampleWrap = new BinWrapper[Sample] {
    override def fetch(any: Any): Option[Sample] = scala.util.Try {
      Value.getFromRecordObject(any) match {
        case m: MapValue => m.getObject match {
          case ms: java.util.Map[String@unchecked, Any@unchecked] => Sample(ms("name").toString, ms("i").toString.toInt)
        }
      }
    }.toOption
  }

  /* This is custom wrapper for Bin of type Map[Sample, String]
     For example for Map(Sample(t1,3) -> v1, Sample(t2,2) -> v2, Sample(t3,1) -> v3) Bin will
     be stored like com.aerospike.client.Value.MapValue, where keys are Sample(...).toString()
     So inside com.aerospike.client.Record it will look like this:
     bins:(BIN_NAME:{Sample(t1,3)=v1, Sample(t2,2)=v2, Sample(t3,1)=v3})
     And if you want to get your Sample type keys to be unwrapped properly you need to write fetch() function as it is shown below
  */
  implicit val sampleMapWrap = new BinWrapper[Map[Sample, String]] {
    val rex = "Sample\\((\\w+)\\,(\\d+)\\)"
    val trRex = rex.r

    override def toValue(v: Map[Sample, String]): MapValue =
      new MapValue(v.map { case (sample, value) => sample.toString -> value })

    override def fetch(any: Any): Option[Map[Sample, String]] = Value.getFromRecordObject(any) match {
      case m: MapValue => m.getObject match {
        case ms: java.util.Map[Any@unchecked, String@unchecked] => scala.util.Try(ms.asScala.map {
          case (tr, v) if tr.toString.matches("Sample\\((\\w+)\\,(\\d+)\\)") =>
            tr.toString match {
              case trRex(n, i) => Sample(n, i.toInt) -> v
            }
        }.toMap).toOption
        case _ => None
      }
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

  /* Note, Aerospikes AQL is good for values with String types. So if you want to store Map with key of any other type - you will see nothing in terminal.
     But you can use function, which gets that value by key (for example getMapIS(...) below) and print it to be sure it all works fine.
     An example for Map[Int, String] is in SampleApp.scala:
       myObj.putMapIS("mapIntString", SingleBin("mapISName", Map(9 -> "v1", 2 -> "v2", 3 -> "v3")))
   */

  def putMapIS(k: String, a: SingleBin[Map[Int, String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[Int, String]](CallKB.Put, k, a)

  def putMapSI(k: String, a: SingleBin[Map[String, Int]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Int]](CallKB.Put, k, a)

  def putMapLong(k: String, a: SingleBin[Map[String, Long]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Long]](CallKB.Put, k, a)

  def putMapFloat(k: String, a: SingleBin[Map[String, Float]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Float]](CallKB.Put, k, a)

  def putMapDouble(k: String, a: SingleBin[Map[String, Double]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Map[String, Double]](CallKB.Put, k, a)

  /* HList is stored as Map[String, Any] in Aerospike's MapValue.
     For example "hlist" :: 2 :: 3 :: HNil will be stored as Map("0" -> "hlist", "1" -> 2, "2" -> 3)
  */
  def putHList(k: String, a: SingleBin[String :: Int :: Int :: HNil])(implicit e: ExecutionContext): Future[Unit] =
  spike.callKB[String, String :: Int :: Int :: HNil](CallKB.Put, k, a)

  def putHList2(k: String, a: SingleBin[String :: Int :: Float :: List[String] :: List[Int] :: HNil])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, String :: Int :: Float :: List[String] :: List[Int] :: HNil](CallKB.Put, k, a)

  def putListSt(k: String, a: SingleBin[List[String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[String]](CallKB.Put, k, a)

  def putListInt(k: String, a: SingleBin[List[Int]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Int]](CallKB.Put, k, a)

  def putListLong(k: String, a: SingleBin[List[Long]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Long]](CallKB.Put, k, a)

  def putListFloat(k: String, a: SingleBin[List[Float]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Float]](CallKB.Put, k, a)

  def putListDouble(k: String, a: SingleBin[List[Double]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Double]](CallKB.Put, k, a)

  def putListBoolean(k: String, a: SingleBin[List[Boolean]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, List[Boolean]](CallKB.Put, k, a)

  def putSample(k: String, a: SingleBin[Sample])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Sample](CallKB.Put, k, a)

  def putTuple(k: String, a: SingleBin[(String, Long, Double)])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, (String, Long, Double)](CallKB.Put, k, a)

  def putSeqArrayBuffer(k: String, a: SingleBin[Seq[ArrayBuffer[Double]]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Seq[ArrayBuffer[Double]]](CallKB.Put, k, a)

  def putArrayByte(k: String, a: SingleBin[Array[Byte]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Byte]](CallKB.Put, k, a)

  def putArrayBoolean(k: String, a: SingleBin[Array[Boolean]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Boolean]](CallKB.Put, k, a)

  def putArrayString(k: String, a: SingleBin[Array[String]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[String]](CallKB.Put, k, a)

  def putArrayInt(k: String, a: SingleBin[Array[Int]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Int]](CallKB.Put, k, a)

  def putArrayLong(k: String, a: SingleBin[Array[Long]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Long]](CallKB.Put, k, a)

  def putArrayFloat(k: String, a: SingleBin[Array[Float]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Float]](CallKB.Put, k, a)

  def putArrayDouble(k: String, a: SingleBin[Array[Double]])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Array[Double]](CallKB.Put, k, a)

  def putByteSegment(k: String, a: SingleBin[ByteSegment])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, ByteSegment](CallKB.Put, k, a)

  def getString(k: String)(implicit e: ExecutionContext): Future[String] = spike.getByKey[String, String](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getInt(k: String)(implicit e: ExecutionContext): Future[Int] = spike.getByKey[String, Int](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getFloat(k: String)(implicit e: ExecutionContext): Future[Float] = spike.getByKey[String, Float](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getDouble(k: String)(implicit e: ExecutionContext): Future[Double] = spike.getByKey[String, Double](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getBoolean(k: String)(implicit e: ExecutionContext): Future[Boolean] = spike.getByKey[String, Boolean](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getShort(k: String)(implicit e: ExecutionContext): Future[Short] = spike.getByKey[String, Short](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getLong(k: String)(implicit e: ExecutionContext): Future[Long] = spike.getByKey[String, Long](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getChar(k: String)(implicit e: ExecutionContext): Future[Char] = spike.getByKey[String, Char](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getByte(k: String)(implicit e: ExecutionContext): Future[Byte] = spike.getByKey[String, Byte](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getHList(k: String)(implicit e: ExecutionContext): Future[String :: Int :: Int :: HNil] = spike
    .getByKey[String, String :: Int :: Int :: HNil](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getHList2(k: String)(implicit e: ExecutionContext): Future[String :: Int :: Float :: List[String] :: List[Int] :: HNil] = spike
    .getByKey[String, String :: Int :: Float :: List[String] :: List[Int] :: HNil](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getSample(k: String)(implicit e: ExecutionContext): Future[Sample] = spike
    .getByKey[String, Sample](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getTuple(k: String)(implicit e: ExecutionContext): Future[(String, Long, Double)] = spike
    .getByKey[String, (String, Long, Double)](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListSt(k: String)(implicit e: ExecutionContext): Future[List[String]] = spike.getByKey[String, List[String]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListInt(k: String)(implicit e: ExecutionContext): Future[List[Int]] = spike.getByKey[String, List[Int]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListLong(k: String)(implicit e: ExecutionContext): Future[List[Long]] = spike.getByKey[String, List[Long]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListFloat(k: String)(implicit e: ExecutionContext): Future[List[Float]] = spike.getByKey[String, List[Float]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListDouble(k: String)(implicit e: ExecutionContext): Future[List[Double]] = spike.getByKey[String, List[Double]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getListBoolean(k: String)(implicit e: ExecutionContext): Future[List[Boolean]] = spike.getByKey[String, List[Boolean]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getSeqArrayBuffer(k: String)(implicit e: ExecutionContext): Future[Seq[ArrayBuffer[Double]]] = spike.getByKey[String, Seq[ArrayBuffer[Double]]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayByte(k: String)(implicit e: ExecutionContext): Future[Array[Byte]] = spike.getByKey[String, Array[Byte]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayString(k: String)(implicit e: ExecutionContext): Future[Array[String]] = spike.getByKey[String, Array[String]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayInt(k: String)(implicit e: ExecutionContext): Future[Array[Int]] = spike.getByKey[String, Array[Int]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayLong(k: String)(implicit e: ExecutionContext): Future[Array[Long]] = spike.getByKey[String, Array[Long]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayFloat(k: String)(implicit e: ExecutionContext): Future[Array[Float]] = spike.getByKey[String, Array[Float]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayDouble(k: String)(implicit e: ExecutionContext): Future[Array[Double]] = spike.getByKey[String, Array[Double]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getArrayBoolean(k: String)(implicit e: ExecutionContext): Future[Array[Boolean]] = spike.getByKey[String, Array[Boolean]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getByteSegment(k: String)(implicit e: ExecutionContext): Future[ByteSegment] = spike.getByKey[String, ByteSegment](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMap(k: String)(implicit e: ExecutionContext): Future[Map[String, String]] = spike.getByKey[String, Map[String, String]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapIS(k: String)(implicit e: ExecutionContext): Future[Map[Int, String]] = spike.getByKey[String, Map[Int, String]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapSI(k: String)(implicit e: ExecutionContext): Future[Map[String, Int]] = spike.getByKey[String, Map[String, Int]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapLong(k: String)(implicit e: ExecutionContext): Future[Map[String, Long]] = spike.getByKey[String, Map[String, Long]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapFloat(k: String)(implicit e: ExecutionContext): Future[Map[String, Float]] = spike.getByKey[String, Map[String, Float]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapDouble(k: String)(implicit e: ExecutionContext): Future[Map[String, Double]] = spike.getByKey[String, Map[String, Double]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

  def getMapSimpleString(k: String)(implicit bC: BinWrapper[Map[Sample, String]], e: ExecutionContext): Future[Map[Sample, String]] = spike
    .getByKey[String, Map[Sample, String]](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))

}