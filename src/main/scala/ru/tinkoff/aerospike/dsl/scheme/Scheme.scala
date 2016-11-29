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

package ru.tinkoff.aerospike.dsl.scheme

import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikemacro.converters.KeyWrapper
import ru.tinkoff.aerospikemacro.domain.DBCredentials


/**
  * @author MarinaSigaeva 
  * @since 26.09.16
  */

/* This trait will work with one key type and different types of Bins */
trait Scheme[K] {
  val spike: SpikeImpl
  implicit val dbc: DBCredentials
//  implicit val kw: KeyWrapper[K]

}

/* This trait will work with one key type and one Bin type */
trait KBScheme[K, B] {
  val spike: SpikeImpl
  implicit val dbc: DBCredentials
  //implicit val kw: KeyWrapper[K]
}