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

import com.aerospike.client.Key
import com.aerospike.client.async.IAsyncClient
import com.aerospike.client.large.{LargeList, LargeMap, LargeSet, LargeStack}
import com.aerospike.client.policy.WritePolicy

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait CollectionsProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def getLargeList(policy: WritePolicy, key: Key, binName: String): Future[LargeList] =
    Future(client.getLargeList(policy, key, binName))

  def getLargeSet(policy: WritePolicy, key: Key, binName: String, userModule: String): Future[LargeSet] =
    Future(client.getLargeSet(policy, key, binName, userModule))

  def getLargeStack(policy: WritePolicy, key: Key, binName: String, userModule: String): Future[LargeStack] =
    Future(client.getLargeStack(policy, key, binName, userModule))

  def getLargeMap(policy: WritePolicy, key: Key, binName: String, userModule: String): Future[LargeMap] =
    Future(client.getLargeMap(policy, key, binName, userModule))

}
