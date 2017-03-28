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
package ru.tinkoff.aerospike.parser

import ru.tinkoff.aerospike.proto.ArrayParser
import ru.tinkoff.aerospikemacro.converters.BinWrapper
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}

import scala.collection.immutable.Map
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import ru.tinkoff.aerospike.proto.ArrayParser

/**
  * @author MarinaSigaeva
  * @since 23.03.17
  */

object ProtoBinWrapper {

  implicit def materializeProtoWrapper[T](implicit ap: ArrayParser[T]): BinWrapper[T] = macro materializeProto[T]

  def materializeProto[T: c.WeakTypeTag](c: blackbox.Context)(ap: c.Expr[ArrayParser[T]]): c.Expr[BinWrapper[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    println("ProtoBin created " + tpe)
    val one = weakTypeOf[(String, T)]
    val singleton = weakTypeOf[SingleBin[T]]
    val multi = weakTypeOf[MBin[T]]
    val many = weakTypeOf[Map[String, T]]
    val out = weakTypeOf[(Map[String, Option[T]], Int, Int)]
    val tpeSt = q"${tpe.toString}"


    val pack = q"${tpe.typeConstructor}" // + "parseFrom(arr)"
    //val parseArray = ValDef(tpe.typeSymbol.fullName.dropRight(tpeLen) + "parseFrom(arr)")
    val importp = q"""import $pack.parseFrom"""
    val b2 = PackageDef(RefTree(q"zxc", TermName("treeName")), List(q"abc"))

    val kreates = c.Expr[BinWrapper[T]] {
      q"""

      import com.aerospike.client.{Bin, Record, Value}
      import scala.collection.JavaConversions._
      import scala.collection.JavaConverters._
      import scala.language.experimental.macros
      import ru.tinkoff.aerospikemacro.converters._
      import com.aerospike.client.Value.BytesValue

      new BinWrapper[$tpe] {
        override def toValue(v: $tpe): Value = new BytesValue(v.toByteArray)
        override def apply(one: $one): Bin = if (one._1.length > 14) throwE("Current limit for bean name is 14 characters") else gen(one)
        override def apply(many: $multi): List[Bin] = many.asOne.view.map(apply).toList
        override def apply(one: $singleton): Bin = apply((one.name, one.value))
        override def apply(many: $many): List[Bin] = many.view.flatMap(one => scala.util.Try(apply(one)).toOption).toList
        override def apply(r: Record): $out = {
           val outValue: Map[String, Option[$tpe]] = {
           val jMap = r.bins.view collect {
            case (name, bt: Any) =>
            val res = fetch(bt)
            if (res.isEmpty && r.bins.nonEmpty) throwClassCast($tpeSt) else name -> res
           }
          jMap.toMap
          }

         (outValue, r.generation, r.expiration)
        }

        override def fetch(any: Any): Option[$tpe] = Try {
          Value.getFromRecordObject(any) match {
            case b: BytesValue => b.getObject match {
            case arr: Array[Byte] => ap.parse(arr)
          }
         }
        }.toOption
      }

    """
    }

    println(kreates)
    kreates
  }
}

