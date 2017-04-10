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
import ru.tinkoff.aerospikemacro.printer.Printer
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.language.experimental.macros

/**
  * @author MarinaSigaeva
  * @since 26.10.16
  */
object KBSampleApp extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  val client = AClient.client
  val spike  = new SpikeImpl(client)

  val myObj = KBSampleScheme(spike)

  private val oneValue   = 2
  private val manyValues = Map("aName" -> 2, "bName" -> 13)
  myObj.putOne("oneKey", SingleBin("oneName", oneValue))
  myObj.putMany("manyKey", MBin(manyValues))

  val one = Await.result(myObj.getOne("oneKey"), Inf)
  assert(one._2.contains(oneValue))
  Printer.printNameValue(one)

  val many = Await.result(myObj.getMany("manyKey"), Inf)
  assert(many == manyValues.mapValues(Some.apply))
  Printer.printNameValue(many)
}
