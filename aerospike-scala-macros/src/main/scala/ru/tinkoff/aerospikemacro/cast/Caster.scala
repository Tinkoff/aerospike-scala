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

package ru.tinkoff.aerospikemacro.cast

import shapeless.syntax.std.tuple._

/**
  * @author MarinaSigaeva
  * @since 27.10.16
  */
object Caster {
  type TP = Any with Product

  def castHListElements(allElems: List[Any], typeStr: String): List[Any] = {
    val need = List("Boolean", "Float", "Char", "Int", "Short", "Byte")
    val types = typeStr
      .replaceAll("""shapeless.::""", "")
      .replace(",shapeless.HNil", "")
      .toCharArray
      .filter(e => e != '[' & e != ']')
      .mkString
      .split(",")

    (for (i <- types.indices) yield {
      if (need.contains(types(i))) cast(allElems(i), types(i)) else allElems(i)
    }).toList
  }

  def cast(elem: Any, desc: String): Any = {
    elem match {
      case long: java.lang.Long if desc == "Boolean" => long == 1
      case long: java.lang.Long if desc == "Int"     => long.toInt
      case long: java.lang.Long if desc == "Short"   => long.toShort
      case long: java.lang.Long if desc == "Byte"    => long.toByte
      case dbl: java.lang.Double if desc == "Float"  => dbl.toFloat
      case str: java.lang.String if desc == "Char" =>
        str.toString.toCharArray.headOption
      case _ => elem
    }
  }

  def castTuple(elems: Map[Any, Any], types: List[String]): Option[TP] = {
    val casted = types.indices.map(i => cast(elems(i.toString), types(i))).toList
    casted.length match {
      case l if l > 0 && l < 23 => scala.util.Try(buildTuple(casted)).toOption
      case _                    => None
    }
  }

  def buildTuple(elems: List[Any]): TP = {
    (for (i <- 1 until elems.length) yield Tuple1(elems.head) :+ elems(i)).toList
  }
}
