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

package ru.tinkoff.aerospikescala.domain

/**
  * @author MarinaSigaeva 
  * @since 15.11.16
  */
trait ABin[B]

case class SingleBin[B](name: String, value: B) extends ABin[B]

case class MBin[B](values: Map[String, B]) extends ABin[B] {
  def asOne: List[SingleBin[B]] = values.view.map(v => SingleBin(v._1, v._2)).toList

}

case class ByteSegment(bytes: Array[Byte], offset: Int, length: Int)