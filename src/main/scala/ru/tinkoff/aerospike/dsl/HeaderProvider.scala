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
import com.aerospike.client.listener.{RecordArrayListener, RecordListener, RecordSequenceListener}
import com.aerospike.client.policy.{BatchPolicy, Policy}
import com.aerospike.client.{Key, Record}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait HeaderProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def getHeader(policy: Policy, key: Key): Future[Record] = Future(client.getHeader(policy, key))

  def getHeader(policy: BatchPolicy, keys: Array[Key]): Future[Array[Record]] = Future(client.getHeader(policy, keys))

  def getHeader(policy: Policy, listener: RecordListener, key: Key): Future[Unit] = Future(client.getHeader(policy, listener, key))

  def getHeader(policy: BatchPolicy, listener: RecordArrayListener, keys: Array[Key]): Future[Unit] =
    Future(client.getHeader(policy, listener, keys))

  def getHeader(policy: BatchPolicy, listener: RecordSequenceListener, keys: Array[Key]): Future[Unit] =
    Future(client.getHeader(policy, listener, keys))

}
