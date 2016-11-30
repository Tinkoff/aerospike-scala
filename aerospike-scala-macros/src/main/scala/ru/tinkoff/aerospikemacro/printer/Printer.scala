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

package ru.tinkoff.aerospikemacro.printer

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * @author MarinaSigaeva 
  * @since 21.10.16
  */
object Printer {
  def printNameValue[T](x: T): Unit = macro impl[T]

  def impl[R](c: blackbox.Context)(x: c.Tree): c.Tree = {
    import c.universe._
    val tpe = weakTypeOf[R]

    val name = x match {
      case Select(_, TermName(s)) => s
      case _ => ""
    }

    val isArray = tpe.widen.typeSymbol.name.eq(TypeName("Array"))
    println("isArray " + isArray)

    q"""
       println("-"*20)
       println($name + " => " + $x)
     """
  }
}