/*
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

import java.util

import com.aerospike.client.BatchRead
import org.scalatest.{FlatSpec, Matchers}
import ru.tinkoff.aerospike.dsl.batchread.BatchReadWrapper
import ru.tinkoff.aerospikemacro.domain.DBCredentials

import scala.collection.JavaConversions._

/**
  * @author MarinaSigaeva 
  * @since 21.09.16
  */
class BatchReadWrapperTest extends FlatSpec with Matchers {

  def getList(kws: List[BatchReadWrapper]): util.List[BatchRead] = {
    kws.view.map { kw => kw.apply }.toList
  }

  it should "create BatchReads of different Key types" in {

    val b1 = new BatchReadWrapper {
      val keyValue = "str"
      val binNames = Array("s1", "s2")
      implicit val dbc = DBCredentials("test", "test")
    }
    val b2 = new BatchReadWrapper {
      val keyValue = 2
      implicit val dbc = DBCredentials("ns", "setName")
      val binNames = Array("s3", "s4")
    }

    val brs = getList(List(b1, b2))
    brs(0).key.namespace shouldBe "test"
    brs(0).key.setName shouldBe "test"
    brs(0).binNames shouldBe Array("s1", "s2")

    brs(1).key.namespace shouldBe "ns"
    brs(1).key.setName shouldBe "setName"
    brs(1).binNames shouldBe Array("s3", "s4")

  }

}
*/
