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

import com.aerospike.client.Value.StringValue
import com.aerospike.client.listener.{ExistsArrayListener, ExistsSequenceListener, RecordSequenceListener, _}
import com.aerospike.client.{Language, ScanCallback, _}
import com.aerospike.client.policy.{InfoPolicy, _}
import com.aerospike.client.query.{RecordSet, ResultSet, Statement}
import com.aerospike.client.task.{ExecuteTask, RegisterTask}
import com.github.danymarialee.mock.MockAerospike
import scala.collection.JavaConversions._


/**
  * Created by danylee on 30/11/16.
  */
object ACMock {

  val m1: java.util.Map[String, AnyRef] = Map("operateBinName" -> new StringValue("operate"))
  val record1 = new Record(m1, 100, 12)
  val s1 = new StringValue("execute")
  val zMock = new MockAerospike()
  val exTask = zMock.et1
  val regTask = zMock.rt1

  def spikeMock = new MockAerospike {

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

    override def execute(policy: WritePolicy, key: Key, packageName: String, functionName: String, args: Value*): Object = s1

    override def execute(policy: WritePolicy, statement: Statement, packageName: String, functionName: String, functionArgs: Value*): ExecuteTask = exTask

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

    override def registerUdfString(policy: Policy, code: String, serverPath: String, language: Language): RegisterTask = regTask


  }
}
