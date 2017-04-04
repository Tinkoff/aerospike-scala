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
import ru.tinkoff.aerospikemacro.domain.{DBCredentials, WrapperException}
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

  def apply(k: KT): Key = new Key(dbName, tableName, toValue(k))

  def toValue(v: KT): Value = Value.get(v) match {
    case n: NullValue => throw new WrapperException { val msg = "You need to write your own toValue function in KeyWrapper" }
    case other => other
  }
}


object KeyWrapper {
  import Utils._

  implicit def materializeK[T](implicit dbc: DBCredentials): KeyWrapper[T] = macro implK[T]

  def implK[T: c.WeakTypeTag](c: Context)(dbc: c.Expr[DBCredentials]): c.Expr[KeyWrapper[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]

    val db = reify(dbc.splice.namespace)
    val tableName = reify(dbc.splice.setname)
    val tpeName = q"${tpe.typeSymbol.fullName}"

    val err =
      q"""throw new IllegalArgumentException(
         "You need to write your own toValue function in KeyWrapper implicit for type " + $tpeName) """

    val toDBVlue = pickValue(c)

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
        override def toValue(v: $tpe): Value = $toDBVlue
      }
     """
    }
  }

  def create[T](dbc: DBCredentials): KeyWrapper[T] = macro implK[T]

}
