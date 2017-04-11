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
      case long: java.lang.Long if desc == "Int" => long.toInt
      case long: java.lang.Long if desc == "Short" => long.toShort
      case long: java.lang.Long if desc == "Byte" => long.toByte
      case dbl: java.lang.Double if desc == "Float" => dbl.toFloat
      case str: java.lang.String if desc == "Char" =>
        str.toString.toCharArray.headOption
      case _ => elem
    }
  }


}

object Tuplify {

  val m = Map(
    2 -> { ls: List[Any] => (ls.head, ls(1)) },
    3 -> { ls: List[Any] => (ls.head, ls(1), ls(2)) },
    4 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3)) },
    5 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4)) },
    6 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5)) },
    7 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6)) },
    8 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7)) },
    9 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8)) },
    10 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9)) },
    11 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10)) },
    12 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11)) },
    13 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12)) },
    14 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13)) },
    15 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14)) },
    16 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15)) },
    17 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16)) },
    18 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16), ls(17)) },
    19 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16), ls(17), ls(18)) },
    20 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16), ls(17), ls(18), ls(19)) },
    21 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16), ls(17), ls(18), ls(19), ls(20)) },
    22 -> { ls: List[Any] => (ls.head, ls(1), ls(2), ls(3), ls(4), ls(5), ls(6), ls(7), ls(8), ls(9), ls(10), ls(11), ls(12), ls(13), ls(14), ls(15), ls(16), ls(17), ls(18), ls(19), ls(20), ls(21)) }
  )

}

