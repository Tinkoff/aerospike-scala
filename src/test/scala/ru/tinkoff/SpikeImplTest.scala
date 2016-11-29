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

package ru.tinkoff


import com.aerospike.client.Operation.Type
import com.aerospike.client.Value.StringValue
import com.aerospike.client.listener.{ExistsSequenceListener, _}
import com.aerospike.client._
import com.aerospike.client.policy._
import com.aerospike.client.query._
import com.aerospike.client.task.{ExecuteTask, RegisterTask}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import com.aerospike.client.Value.MapValue
import com.github.danymarialee.mock._
import ru.tinkoff.aerospike.dsl._
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}

import scala.language.experimental.macros
import shapeless.{HList, _}
import shapeless.HList.hlistOps
import syntax.std.traversable._

import scala.collection.JavaConversions._
import ru.tinkoff.aerospikemacro.converters._
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}
import ru.tinkoff.aerospike.dsl.errors.AerospikeDSLError
import ru.tinkoff.aerospikemacro.domain.DBCredentials

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * @author MarinaSigaeva
  * @since 08.09.16
  */
class SpikeImplTest extends FlatSpec with Matchers with MockitoSugar with ScalaFutures {

  trait mocks {
    val m1: java.util.Map[String, AnyRef] = Map("operateBinName" -> new StringValue("operate"))
    val record1 = new Record(m1, 100, 12)
    val s1 = new StringValue("execute")

    val spikeMock = new MockAerospike {
      override def put(policy: WritePolicy, key: Key, bins: Bin*): Unit = {}

      override def append(policy: WritePolicy, key: Key, bins: Bin*): Unit = {}

      override def prepend(policy: WritePolicy, key: Key, bins: Bin*): Unit = {}

      override def operate(policy: WritePolicy, key: Key, operations: Operation*): Record = record1

      override def operate(policy: WritePolicy, listener: RecordListener, key: Key, operations: Operation*): Unit = {}

      override def delete(policy: WritePolicy, listener: DeleteListener, key: Key): Unit = {}

      override def delete(policy: WritePolicy, key: Key): Boolean = true

      override def touch(policy: WritePolicy, listener: WriteListener, key: Key): Unit = {}

      override def touch(policy: WritePolicy, key: Key): Unit = {}

      override def execute(policy: WritePolicy, listener: ExecuteListener, key: Key,
                           packageName: String, functionName: String, functionArgs: Value*): Unit = {}

      override def execute(policy: WritePolicy, key: Key, packageName: String, functionName: String, args: Value*): Any = s1

      override def execute(policy: WritePolicy, statement: Statement, packageName: String, functionName: String, functionArgs: Value*): ExecuteTask = et1

      override def exists(policy: Policy, listener: ExistsListener, key: Key): Unit = {}

      override def exists(policy: Policy, key: Key): Boolean = true

      override def exists(policy: BatchPolicy, listener: ExistsArrayListener, keys: Array[Key]): Unit = {}

      override def exists(policy: BatchPolicy, listener: ExistsSequenceListener, keys: Array[Key]): Unit = {}

      override def exists(policy: BatchPolicy, keys: Array[Key]): Array[Boolean] = Array(true)

      override def query(policy: QueryPolicy, listener: RecordSequenceListener, statement: Statement): Unit = {}

      override def query(policy: QueryPolicy, statement: Statement): RecordSet = null

      //= new RecordSet(queryExecutor, 1)
      override def queryAggregate(policy: QueryPolicy, statement: Statement, packageName: String, functionName: String, functionArgs: Value*): ResultSet = null

      override def queryAggregate(policy: QueryPolicy, statement: Statement): ResultSet = null

      override def scanAll(policy: ScanPolicy, listener: RecordSequenceListener, namespace: String, setName: String, binNames: String*): Unit = {}

      override def scanAll(policy: ScanPolicy, namespace: String, setName: String, callback: ScanCallback, binNames: String*): Unit = {}

      override def removeUdf(policy: InfoPolicy, serverPath: String): Unit = {}

      override def registerUdfString(policy: Policy, code: String, serverPath: String, language: Language): RegisterTask = rt1


    }

    val spikeDao = new SpikeImpl(spikeMock)
  }

  case class Cat(name: String)

  //bin will look like: Bin("binName", "binValue")
  "SpikeImpl" should "call[K,B] put method for one element" in new mocks {
    implicit val dbc = DBCredentials("test", "test")
    spikeDao.callKB[String, String](CallKB.Put, "StrKey", SingleBin("binName", "binValue")).futureValue shouldBe()
  }

  //bin will look like: List(Bin("binName", "binValue"), Bin("binName2", "binValue"), Bin("binName3", "binValue"))
  it should "call[K,B] prepend method for many elements same type" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callKB[String, String](CallKB.Prepend, "StrKey", MBin(
      Map("binName1" -> "binValue",
        "binName2" -> "binValue",
        "binName3" -> "binValue")))
      .futureValue shouldBe()
  }

  //bin will look like: Bin("binName", ListValue(List("binValue1", "binValue2")))
  it should "call[K,B] append method for seq of elements as a value" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callKB[String, Seq[String]](CallKB.Append, "StrKey",
      SingleBin("binName", Seq("binValue1", "binValue2"))).futureValue shouldBe()
  }

  it should "call[K] operate" in new mocks {

    import KeyWrapper._

    implicit val dbc = DBCredentials("ns", "setName")
    val stKeyWrapper = create[String](dbc)

    val ops = List(new Operation(Type.WRITE, "operateBinName", new StringValue("operate")), Operation.get("operateBinName"))

    spikeDao.callK(CallK.Operate, "strOperateKey", ops).futureValue shouldBe record1

    spikeDao.callK(CallK.Operate, "strOperateKey", any = (ops, ReadHandler(stKeyWrapper("strOperateKey"), record1)))
      .futureValue shouldBe {}
  }

  it should "call[K] delete" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callK(CallK.Delete, "strDeleteKey", DeleteHandler()).futureValue shouldBe {}
    spikeDao.callK(CallK.Delete, "strDeleteKey").futureValue shouldBe true
  }

  it should "call[K] touch" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callK(CallK.Touch, "strTouchKey", WriteHandler()).futureValue shouldBe {}
    spikeDao.callK(CallK.Touch, 3).futureValue shouldBe {}
  }

  it should "call[K] execute" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callK(CallK.Execute, "strExecKey", Param1("pkg", "fName",
      List(new StringValue("str")), None, Option(ExecuteHandler()))).futureValue shouldBe {}

    spikeDao.callK(CallK.Execute, "strExecKey", Param1("pkg", "fName", List(new StringValue("str")))).futureValue shouldBe s1
  }

  it should "call[K] exists" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callK(CallK.Exists, "strKey", ExistsHandler()).futureValue shouldBe {}
    spikeDao.callK(CallK.Exists, "strKey").futureValue shouldBe true
  }

  it should "call[K]s exists" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.callKs(CallKs.Exists, Array("sk1", "sk2"), ExistsArrayHandler()).futureValue shouldBe {}
    spikeDao.callKs(CallKs.Exists, Array("sk1", "sk2"), ExistsSequenceHandler()).futureValue shouldBe {}
    spikeDao.callKs(CallKs.Exists, Array("sk1", "sk2")).futureValue shouldBe Array(true)
  }

  it should "call execute" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.Execute, Param1("pkg", "fName", List(new StringValue("str")), Option(new Statement()))).futureValue shouldBe spikeMock.et1
  }

  it should "call query" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.Query, Param2(new Statement(), Some(RecordSequenceHandler()))).futureValue shouldBe {}
    spikeDao.call(Call.Query, Param2(new Statement())).futureValue shouldBe null.asInstanceOf[ResultSet] //spikeMock.rs
  }

  it should "call queryAggregate" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.QueryAggregate, Param1("pName", "fName", Nil, Some(new Statement()))).futureValue shouldBe null.asInstanceOf[ResultSet]
    spikeDao.call(Call.QueryAggregate, new Statement()).futureValue shouldBe null.asInstanceOf[ResultSet]
  }

  it should "call scanAll" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.ScanAll, Param3("nSpace", "setName", Nil, Some(ScanCallbackImpl()))).futureValue shouldBe {}
    spikeDao.call(Call.ScanAll, Param4("nSpace", "setName", Nil, Some(RecordSequenceHandler()))).futureValue shouldBe {}
  }

  it should "call removeUdf" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.RemoveUdf, "serverPath").futureValue shouldBe {}
  }

  it should "call registerUdfString" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    spikeDao.call(Call.RegisterUdfString, Param5("10", "serverPath", Language.LUA)).futureValue shouldBe spikeMock.rt1
  }

  it should "throw Unsupported type or action exception" in new mocks {
    implicit val dbc = DBCredentials("test", "test")

    intercept[AerospikeDSLError](spikeDao.callK[String](CallK.Operate, "StrKey", 2).futureValue)
      .message shouldBe "Unsupported type class java.lang.Integer or action Operate is not available for this type. You can use: Operate, Delete, Touch, Execute, Exists"
  }

}
