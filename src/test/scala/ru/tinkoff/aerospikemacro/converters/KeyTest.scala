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

import com.aerospike.client.Key
import org.scalatest.{FlatSpec, Matchers}
import ru.tinkoff.aerospikemacro.domain.DBCredentials

/**
  * @author MarinaSigaeva 
  * @since 08.09.16
  */
class KeyTest extends FlatSpec with Matchers {
  implicit val dbc = DBCredentials("test", "test")

  def getKey[K](k: K)(implicit kC: KeyWrapper[K]): Key = kC(k)

  case class Cat(name: String, age: Int)

  trait mocks {

    implicit val bValue = new KeyWrapper[Cat] {
      override def apply(cat: Cat): Key = new Key("test", "test", cat.toString)
    }

    val arr = Array(1.toByte, 1.toByte, 1.toByte)
    val stringKey = new Key("test", "test", "StringKey")
    val intKey = new Key("test", "test", 12)
    val longKey = new Key("test", "test", 1L)
    val catKey = new Key("test", "test", Cat("blob", 12).toString)
    val byteArrayKey = new Key("test", "test", arr)
  }

  it should "work with one namespace and " in new mocks {

    getKey("StringKey") shouldBe stringKey
    getKey(12) shouldBe intKey
    getKey(1L) shouldBe longKey
    getKey(arr) shouldBe byteArrayKey
    getKey(Cat("blob", 12)) shouldBe catKey

  }

  trait mocks2 {

    import KeyWrapper._
    def withCustomKey[T](any: T, dbc: DBCredentials): Key = create(dbc)(any)

    val stringKey0 = new Key("test", "test", "StringKey")
    val stringKey1 = new Key("dbName01", "tableName1", "StringKey")
    val stringKey2 = new Key("dbName02", "tableName2", "StringKey")

  }

  it should "work in different namespaces and setNames" in new mocks2 {
    withCustomKey("StringKey", DBCredentials("dbName01", "tableName1")) shouldBe stringKey1
    withCustomKey("StringKey", DBCredentials("dbName02", "tableName2")) shouldBe stringKey2

    //and u can still use default namespace and setName
    getKey("StringKey") shouldBe stringKey0

  }

}