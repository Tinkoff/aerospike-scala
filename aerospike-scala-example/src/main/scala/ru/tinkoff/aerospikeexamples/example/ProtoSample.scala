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

  /* '''aql> select * from test.test'''
   *  [
   *    {
   *      "pDesigner": "0A 0E 4B 61 72 6C 20 4C 61 67 65 72 66 65 6C 64 10 53"
   *    },
   *    {
   *      "pDesigners": "0A 12 0A 0E 4B 61 72 6C 20 4C 61 67 65 72 66 65 6C 64 10 53 0A 19 0A 15 44 69 61 6E 65 20 76 6F 6E 20 46 75 72 73 74 65 6E 62 65 72 67 10 46 0A 15 0A 11 44 6F 6E 61 74 65 6C 6C 61 20 56 65 72 73 61 63 65 10 3D"
   *    }
   *  ]
   */

  db.put("protoDesigner", SingleBin("pDesigner", one))
  db.put("protoDesigners", SingleBin("pDesigners", many))

  val oneDesigner = Await.result(db.get[Designer]("protoDesigner"), Inf)
  val manyDesigners = Await.result(db.get[Designers]("protoDesigners"), Inf)

  /*   --------------------
   *   '''oneDesigner''' => Map(pDesigner -> Some(name: "Karl Lagerfeld"
   *   age: 83
   *   ))
   *   --------------------
   *   '''manyDesigners''' => Map(pDesigners -> Some(designers {
   *     name: "Karl Lagerfeld"
   *     age: 83
   *   }
   *   designers {
   *     name: "Diane von Furstenberg"
   *     age: 70
   *   }
   *   designers {
   *     name: "Donatella Versace"
   *     age: 61
   *   }
   *   ))
   */

  show(oneDesigner)
  show(manyDesigners)
}