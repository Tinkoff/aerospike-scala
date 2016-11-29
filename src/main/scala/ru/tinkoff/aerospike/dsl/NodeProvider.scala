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

import com.aerospike.client.ScanCallback
import com.aerospike.client.async.IAsyncClient
import com.aerospike.client.cluster.Node
import com.aerospike.client.policy.{QueryPolicy, ScanPolicy}
import com.aerospike.client.query.{RecordSet, ResultSet, Statement}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait NodeProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def getNodeNames: Future[List[String]] = Future(client.getNodeNames.asScala.toList)

  def scanNode(policy: ScanPolicy, nodeName: String, namespace: String,
               setName: String, callback: ScanCallback, binNames: String*): Future[Unit] =
    Future(client.scanNode(policy, nodeName, namespace, setName, callback, binNames: _*))

  def scanNode(policy: ScanPolicy, node: Node, namespace: String, setName: String,
               callback: ScanCallback, binNames: String*): Future[Unit] =
    Future(client.scanNode(policy, node, namespace, setName, callback, binNames: _*))

  def queryNode(policy: QueryPolicy, statement: Statement, node: Node): Future[RecordSet] =
    Future(client.queryNode(policy, statement, node))

  def getNodes: Future[Array[Node]] = Future(client.getNodes)

  def queryAggregateNode(policy: QueryPolicy, statement: Statement, node: Node): Future[ResultSet] =
    Future(client.queryAggregateNode(policy, statement, node))

  def getNode(nodeName: String): Future[Node] = Future(client.getNode(nodeName))
}
