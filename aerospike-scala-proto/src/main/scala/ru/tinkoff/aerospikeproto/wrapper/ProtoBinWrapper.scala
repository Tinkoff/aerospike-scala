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
package ru.tinkoff.aerospikeproto.wrapper

import com.aerospike.client.Value
import com.aerospike.client.Value.BytesValue
import com.trueaccord.lenses.Updatable
import com.trueaccord.scalapb.{GeneratedMessage, Message}
import ru.tinkoff.aerospikemacro.converters.BinWrapper

/**
  * @author MarinaSigaeva
  * @since 23.03.17
  */

trait ProtoBinWrapper[T <: GeneratedMessage with Message[T] with Updatable[T]] extends BinWrapper[T] {
  override def toValue(v: T): Value = new BytesValue(v.toByteArray)

  override def fetch(any: Any): Option[T] = scala.util.Try {
    Value.getFromRecordObject(any) match {
      case b: BytesValue => b.getObject match {
        case arr: Array[Byte] => parse(arr)
      }
    }
  }.toOption

  def parse: Array[Byte] => T
}