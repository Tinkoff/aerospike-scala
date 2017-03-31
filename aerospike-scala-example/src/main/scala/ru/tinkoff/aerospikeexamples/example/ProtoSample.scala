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

import ru.tinkoff.aerospikemacro.printer.Printer.{printNameValue => show}
import ru.tinkoff.aerospikemacro.converters.KeyWrapper
import ru.tinkoff.aerospikeproto.wrapper.ProtoBinWrapper
import ru.tinkoff.aerospikescala.domain.SingleBin

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.experimental.macros
import ru.tinkoff.aerospikeexamples.designers.designers.Designer
import ru.tinkoff.aerospikeexamples.designers.designers.Designers
import ProtoBinWrapper._

/**
  * @author MarinaSigaeva 
  * @since 23.03.17
  */
object ProtoSample extends App {

  val db = new ProtoScheme
  implicit val dbc = AClient.dbc

  val one = Designer("Karl Lagerfeld", 83)
  val many = Designers(List(one, Designer("Diane von Furstenberg", 70), Designer("Donatella Versace", 61)))

  db.put("protoDesigner", SingleBin("pDesigner", one))
  db.put("protoDesigners", SingleBin("pDesigners", many))

  val oneDesigner = Await.result(db.get[Designer]("protoDesigner"), Inf)
  val manyDesigners = Await.result(db.get[Designers]("protoDesigners"), Inf)

  show(oneDesigner)
  show(manyDesigners)
}
