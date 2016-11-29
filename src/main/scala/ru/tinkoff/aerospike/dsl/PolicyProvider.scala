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
import com.aerospike.client.policy._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait PolicyProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def getAsyncQueryPolicyDefault: Future[QueryPolicy] = Future(client.getAsyncQueryPolicyDefault)

  def getAsyncReadPolicyDefault: Future[Policy] = Future(client.getAsyncReadPolicyDefault)

  def getAsyncWritePolicyDefault: Future[WritePolicy] = Future(client.getAsyncWritePolicyDefault)

  def getAsyncScanPolicyDefault: Future[ScanPolicy] = Future(client.getAsyncScanPolicyDefault)

  def getAsyncBatchPolicyDefault: Future[BatchPolicy] = Future(client.getAsyncBatchPolicyDefault)

  def getWritePolicyDefault: Future[WritePolicy] = Future(client.getWritePolicyDefault)

  def getReadPolicyDefault: Future[Policy] = Future(client.getReadPolicyDefault)

  def getBatchPolicyDefault: Future[BatchPolicy] = Future(client.getBatchPolicyDefault)

  def getInfoPolicyDefault: Future[InfoPolicy] = Future(client.getInfoPolicyDefault)

  def getQueryPolicyDefault: Future[QueryPolicy] = Future(client.getQueryPolicyDefault)

  def getScanPolicyDefault: Future[ScanPolicy] = Future(client.getScanPolicyDefault)
}
