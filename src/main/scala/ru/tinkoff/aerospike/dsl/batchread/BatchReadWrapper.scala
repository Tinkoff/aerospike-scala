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

package ru.tinkoff.aerospike.dsl.batchread

import com.aerospike.client.BatchRead
import ru.tinkoff.aerospikemacro.converters._
import com.aerospike.client.Key
import ru.tinkoff.aerospikemacro.domain.DBCredentials

import scala.language.experimental.macros


/**
  * @author MarinaSigaeva 
  * @since 21.09.16
  */
trait BatchReadWrapper {
  val keyValue: Any
  val binNames: Array[String]
  implicit val dbc: DBCredentials

  def applyO[S <: Any](k: S)(implicit kW: KeyWrapper[S]): BatchRead = new BatchRead(kW(k), binNames)
  def apply = applyO(keyValue)
}
