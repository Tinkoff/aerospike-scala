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

import ru.tinkoff.aerospike.dsl.scheme.KBScheme
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospikemacro.domain.DBCredentials
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author MarinaSigaeva
  * @since 26.10.16
  */
case class KBSampleScheme(spike: SpikeImpl) extends KBScheme[String, Int] {
  implicit val dbc: DBCredentials = AClient.dbc

  def putOne(k: String, a: SingleBin[Int])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, Int](CallKB.Put, k, a)

  /**
    * Map represents Bin as list of pairs of (bin name -> value).
    * This will put your Map[String, Int] to Aerospike like Key -> List[Bin].
    * For example Map("a" -> 2, "b" -> 13) for key = "k1" will look like this:
    * k1 -> List(Bin("a", 2), Bin("b", 13))
    * */
  def putMany(k: String, a: MBin[Int])(implicit e: ExecutionContext): Future[Unit] =
    spike.callKB[String, Int](CallKB.Put, k, a)

  def getOne(k: String)(implicit e: ExecutionContext): Future[(String, Option[Int])] =
    spike
      .getByKey[String, Int](k, Nil)
      .map { optValue =>
        optValue.flatMap(_._1.find(_._2.nonEmpty)).getOrElse(throw new Exception("No data found"))
      }

  def getMany(k: String)(implicit e: ExecutionContext): Future[Map[String, Option[Int]]] =
    spike
      .getByKey[String, Int](k, Nil)
      .map(optValue => optValue.map(_._1).getOrElse(throw new Exception("No data found")))
}
