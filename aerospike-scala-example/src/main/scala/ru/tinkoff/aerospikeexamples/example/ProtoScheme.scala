/*
 * Copyright (c) 2017 Tinkoff
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

import com.aerospike.client.{Key, Value}
import com.aerospike.client.Value.{BytesValue, StringValue}
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospike.dsl.scheme.Scheme
import ru.tinkoff.aerospikemacro.converters.KeyWrapper
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import java.util.{List => JList, Map => JMap}

import com.aerospike.client.{Bin, Record, Value}
import com.aerospike.client.Value.{BlobValue, ListValue, MapValue, ValueArray}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author MarinaSigaeva 
  * @since 23.03.17
  */
class ProtoScheme extends Scheme[String] {

  import ru.tinkoff.aerospikeexamples.designers.Designer
  import ru.tinkoff.aerospikeexamples.designers.Designers
  import ru.tinkoff.aerospikeexamples.designers.Designer._

  import ru.tinkoff.aerospikeproto.ProtoBinWrapper.materializeBinWrapper

  implicit val dbc = AClient.dbc
  val spike: SpikeImpl = AClient.spikeImpl

  def putDesigner(k: String, a: SingleBin[Designer])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, Designer](CallKB.Put, k, a)

  def getDesigner(k: String)(implicit e: ExecutionContext): Future[Designer] =
    spike.getByKey[String, Designer](k).map(o =>
      o.flatMap(e => e._1.values.filter(_.nonEmpty).head)
        .getOrElse(throw new Exception("No data found")))

 /* def putDesigners(k: String, a: SingleBin[Designers])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, Designers](CallKB.Put, k, a)

  def getDesigners(k: String)(implicit e: ExecutionContext): Future[Designers] =
    spike.getByKey[String, Designers](k).map(o =>
    o.flatMap(e => e._1.values.filter(_.nonEmpty).head).getOrElse(throw new Exception("No data found")))*/

}