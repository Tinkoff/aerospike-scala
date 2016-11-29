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


import shapeless._
import syntax.std.traversable._
import org.junit.Test
import org.junit.Assert._
import ru.tinkoff.aerospikemacro.cast.Caster
import scala.reflect.runtime.universe._


/**
  * @author MarinaSigaeva 
  * @since 27.10.16
  */
class CasterTest {
  val tpeStr = """shapeless.::[String,shapeless.::[Int,shapeless.::[Int,shapeless.HNil]]]"""
  val isTypes = tpeStr.replaceAll("""shapeless.::""", "").replace(",shapeless.HNil", "").toCharArray
    .filter(e => e != '[' & e != ']').mkString.split(",")

  @Test
  def testCast() {
    val expected = Caster.cast(2L, isTypes(1)).asInstanceOf[Int]
    assertTrue(expected == 2)
  }

  @Test
  def testCastHList() {
    val expected = Caster.castHListElements(List("ddd", 2L, 4L), tpeStr).toHList[String :: Int :: Int :: HNil]
    assertTrue(expected.isDefined)
  }

  @Test
  def testCastTuple() {
    import collection.JavaConversions._
    import collection.JavaConverters._

    val hMap: java.util.HashMap[Any, Any] = new java.util.HashMap[Any, Any](Map("0" -> 2, "1" -> "asd"))
    val tpl = weakTypeOf[Tuple2[Int, String]].typeArgs.map(_.toString)
    val expected = Caster.castTuple(hMap.asScala.toMap, tpl)
    assertTrue(expected == Option((2, "asd")))
  }
}
