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

package ru.tinkoff.aerospikemacro.converters

import com.aerospike.client.Value._
import com.aerospike.client.{Key, Value}
import com.typesafe.config.{Config, ConfigFactory}
import ru.tinkoff.aerospikemacro.domain.DBCredentials
import ru.tinkoff.aerospikescala.domain.ByteSegment

import scala.collection.JavaConversions._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.util.{Failure, Success, Try}


/**
  * @author MarinaSigaeva 
  * @since 19.09.16
  */
trait KeyWrapper[KT] {

  val dbName: String = ""
  val tableName: String = ""

  def apply(k: KT): Key

  def toValue(v: KT): Value = v match {
    case ByteSegment(bytes, offset, length) => new ByteSegmentValue(bytes, offset, length)
    case b: Int => new IntegerValue(b)
    case b: Long => new LongValue(b)
    case b: String => new StringValue(b)
    case b: Boolean => new BooleanValue(b)
    case b: Float => new FloatValue(b)
    case b: Double => new DoubleValue(b)
    case b: Array[Byte] => new BytesValue(b)
    case s: scala.collection.immutable.Seq[_] => new ListValue(s)
    case m: Map[_, _] => new MapValue(m)
    case other => Try(Value.get(other)) match {
      case Failure(th) => throw new IllegalArgumentException(
        s"You need to write your own toValue(v: ${other.getClass}): Value function in KeyWrapper implicit")
      case Success(s) => s
    }
  }
}


object KeyWrapper {

  implicit def materializeK[T](implicit dbc: DBCredentials): KeyWrapper[T] = macro implK[T]

  def implK[T: c.WeakTypeTag](c: Context)(dbc: c.Expr[DBCredentials]): c.Expr[KeyWrapper[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]

    val db = reify(dbc.splice.namespace)
    val tableName = reify(dbc.splice.setname)

    c.Expr[KeyWrapper[T]] {
      q"""
      import com.aerospike.client.{Key, Value}
      import collection.JavaConversions._
      import com.aerospike.client.Value._
      import scala.collection.immutable.Seq
      import ru.tinkoff.aerospikescala.domain.ByteSegment
      import scala.util.{Failure, Success, Try}

      new KeyWrapper[$tpe] {
        override val dbName = $db
        override val tableName = $tableName
        def apply(k: $tpe): Key = new Key(dbName, tableName, toValue(k))
      }
     """
    }
  }

  def create[T](dbc: DBCredentials): KeyWrapper[T] = macro implK[T]

}
