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

import java.util

import com.aerospike.client.async.IAsyncClient
import com.aerospike.client.listener._
import com.aerospike.client.policy.{BatchPolicy, Policy}
import com.aerospike.client.{BatchRead, Key, Record}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait GetProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def getS(policy: BatchPolicy, listener: BatchSequenceListener, records: util.List[BatchRead]): Future[Unit] =
    Future(client.get(policy, listener, records))

  def get(policy: BatchPolicy, listener: RecordSequenceListener, keys: Array[Key], binNames: String*): Future[Unit] =
    Future(client.get(policy, listener, keys, binNames: _*))

  def get(policy: BatchPolicy, listener: RecordArrayListener, keys: Array[Key], binNames: String*): Future[Unit] =
    Future(client.get(policy, listener, keys, binNames: _*))

  def get(policy: BatchPolicy, listener: RecordSequenceListener, keys: Array[Key]): Future[Unit] =
    Future(client.get(policy, listener, keys))

  def get(policy: BatchPolicy, listener: RecordArrayListener, keys: Array[Key]): Future[Unit] =
    Future(client.get(policy, listener, keys))

  def get(policy: Policy, listener: RecordListener, key: Key, binNames: String*): Future[Unit] =
    Future(client.get(policy, listener, key, binNames: _*))

  def get(policy: Policy, listener: RecordListener, key: Key): Future[Unit] =
    Future(client.get(policy, listener, key))

  def get(policy: BatchPolicy, listener: BatchListListener, records: util.List[BatchRead]): Future[Unit] =
    Future(client.get(policy, listener, records))

  def get(policy: BatchPolicy, records: util.List[BatchRead]): Future[Unit] =
    Future(client.get(policy, records))

  def get(policy: BatchPolicy, keys: Array[Key], binNames: String*): Array[Option[Record]] =
    client.get(policy, keys, binNames: _*).map(Option.apply)

  def get(policy: BatchPolicy, keys: Array[Key]): Array[Option[Record]] =
    client.get(policy, keys).map(Option.apply)

  def get(policy: Policy, key: Key, binNames: String*): Option[Record] =
    Option(client.get(policy, key, binNames: _*))

  def get(policy: Policy, key: Key): Option[Record] = Option(client.get(policy, key))
}
