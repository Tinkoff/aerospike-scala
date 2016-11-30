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

import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikemacro.converters.KeyWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import scala.language.experimental.macros


/**
  * @author MarinaSigaeva 
  * @since 20.10.16
  *
  *        This will delete all data stored in SampleApp
  */
object CleanUp extends App {

  val client = AClient.client
  val spike = new SpikeImpl(client)
  implicit val dbc = AClient.dbc

  val keys = List("mapKey", "mapSimpleString", "mapStringString", "mapIntString", "mapStringInt", "mapLong", "mapFloat", "mapDouble", "stringKey",
    "intBinKey", "floatBinKey", "doubleBinKey", "boolBinKey", "shortBinKey", "longBinKey", "charBinKey", "byteBinKey",
    "listStBinKey", "listIntKey", "listLongKey", "listFloatKey", "listDoubleKey", "sampleKey", "hListKey", "oneKey",
    "manyKey", "hListKey2", "tuple3Key", "arrayByteKey", "byteSegmKey", "arrayStKey",
    "arrayDoubleKey", "arrayFloatKey", "arrayLongKey", "arrayIntKey")

  val result = for (key <- keys) yield spike.deleteK(key)
  Await.result(Future.sequence(result), Inf)

}
