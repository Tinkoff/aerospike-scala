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

package ru.tinkoff.aerospike.dsl

import com.aerospike.client.async.IAsyncClient
import com.aerospike.client.listener.{RecordSequenceListener, _}
import com.aerospike.client.policy.{QueryPolicy, WritePolicy, _}
import com.aerospike.client.query._
import com.aerospike.client.task.{ExecuteTask, RegisterTask}
import com.aerospike.client.{Bin, Key, Record, _}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait MainProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def scanAll(policy: ScanPolicy, listener: RecordSequenceListener, namespace: String,
              setName: String, binNames: String*): Future[Unit] =
    Future(client.scanAll(policy, listener, namespace, setName, binNames: _*))


  def scanAll(policy: ScanPolicy, namespace: String, setName: String,
              callback: ScanCallback, binNames: String*): Future[Unit] =
    Future(client.scanAll(policy, namespace, setName, callback, binNames: _*))

  def operate(policy: WritePolicy, listener: RecordListener,
              key: Key, operations: Operation*): Future[Unit] =
    Future(client.operate(policy, listener, key, operations: _*))

  def prepend(policy: WritePolicy, listener: WriteListener, key: Key, bins: Bin*): Future[Unit] =
    Future(client.prepend(policy, listener, key, bins: _*))

  def put(policy: WritePolicy, listener: WriteListener, key: Key, bins: Bin*): Future[Unit] =
    Future(client.put(policy, listener, key, bins: _*))

  def execute(policy: WritePolicy, listener: ExecuteListener, key: Key, packageName: String, functionName: String, functionArgs: Value*): Future[Unit] =
    Future(client.execute(policy, listener, key, packageName, functionName, functionArgs: _*))

  def append(policy: WritePolicy, listener: WriteListener, key: Key, bins: Bin*): Future[Unit] =
    Future(client.append(policy, listener, key, bins: _*))

  def touch(policy: WritePolicy, listener: WriteListener, key: Key): Future[Unit] =
    Future(client.touch(policy, listener, key))

  def delete(policy: WritePolicy, listener: DeleteListener, key: Key): Future[Unit] =
    Future(client.delete(policy, listener, key))

  def add(policy: WritePolicy, listener: WriteListener, key: Key, bins: Bin*): Future[Unit] =
    Future(client.add(policy, listener, key, bins: _*))

  def exists(policy: Policy, listener: ExistsListener, key: Key): Future[Unit] =
    Future(client.exists(policy, listener, key))

  def exists(policy: BatchPolicy, listener: ExistsArrayListener, keys: Array[Key]): Future[Unit] =
    Future(client.exists(policy, listener, keys))

  def exists(policy: BatchPolicy, listener: ExistsSequenceListener, keys: Array[Key]): Future[Unit] =
    Future(client.exists(policy, listener, keys))

  def query(policy: QueryPolicy, listener: RecordSequenceListener, statement: Statement): Future[Unit] =
    Future(client.query(policy, listener, statement))

  def query(policy: QueryPolicy, statement: Statement): Future[RecordSet] =
    Future(client.query(policy, statement))

  def operate(policy: WritePolicy, key: Key, operations: Operation*): Future[Record] =
    Future(client.operate(policy, key, operations: _*))

  def removeUdf(policy: InfoPolicy, serverPath: String): Future[Unit] =
    Future(client.removeUdf(policy, serverPath))

  def prepend(policy: WritePolicy, key: Key, bins: Bin*): Future[Unit] =
    Future(client.prepend(policy, key, bins: _*))

  def registerUdfString(policy: Policy, code: String,
                        serverPath: String, language: Language): Future[RegisterTask] =
    Future(client.registerUdfString(policy, code, serverPath, language))

  def put(policy: WritePolicy, key: Key, bins: Bin*): Future[Unit] =
    Future(client.put(policy, key, bins: _*))

  def execute(policy: WritePolicy, key: Key, packageName: String, functionName: String, args: Value*): Future[AnyRef] =
    Future(client.execute(policy, key, packageName, functionName, args: _*))

  def execute(policy: WritePolicy, statement: Statement, packageName: String, functionName: String, functionArgs: Value*): Future[ExecuteTask] =
    Future(client.execute(policy, statement, packageName, functionName, functionArgs: _*))

  def append(policy: WritePolicy, key: Key, bins: Bin*): Future[Unit] =
    Future(client.append(policy, key, bins: _*))

  def touch(policy: WritePolicy, key: Key): Future[Unit] =
    Future(client.touch(policy, key))

  def delete(policy: WritePolicy, key: Key): Future[Boolean] =
    Future(client.delete(policy, key))

  def add(policy: WritePolicy, key: Key, bins: Bin*): Future[Unit] =
    Future(client.add(policy, key, bins: _*))

  def exists(policy: Policy, key: Key): Future[Boolean] =
    Future(client.exists(policy, key))

  def exists(policy: BatchPolicy, keys: Array[Key]): Future[Array[Boolean]] =
    Future(client.exists(policy, keys))

  def queryAggregate(policy: QueryPolicy, statement: Statement,
                     packageName: String, functionName: String,
                     functionArgs: Value*): Future[ResultSet] =
    Future(client.queryAggregate(policy, statement, packageName, functionName, functionArgs: _*))

  def queryAggregate(policy: QueryPolicy, statement: Statement): Future[ResultSet] =
    Future(client.queryAggregate(policy, statement))

}

