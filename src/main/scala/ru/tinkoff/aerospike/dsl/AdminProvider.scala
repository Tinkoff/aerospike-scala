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

import com.aerospike.client.Language
import com.aerospike.client.admin.{Privilege, Role, User}
import com.aerospike.client.async.IAsyncClient
import com.aerospike.client.policy.{AdminPolicy, Policy}
import com.aerospike.client.query.{IndexCollectionType, IndexType}
import com.aerospike.client.task.{IndexTask, RegisterTask}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by danylee on 11/09/16.
  */
trait AdminProvider {

  def client: IAsyncClient

  implicit val ec: ExecutionContext

  def createRole(policy: AdminPolicy, roleName: String, privileges: util.List[Privilege]): Future[Unit] =
    Future(client.createRole(policy, roleName, privileges))

  def createUser(policy: AdminPolicy, user: String, password: String, roles: util.List[String]): Future[Unit] =
    Future(client.createUser(policy, user, password, roles))

  def createIndex(policy: Policy, namespace: String, setName: String, indexName: String,
                  binName: String, indexType: IndexType): Future[IndexTask] =
    Future(client.createIndex(policy, namespace, setName, indexName, binName, indexType))

  def createIndex(policy: Policy, namespace: String, setName: String, indexName: String,
                  binName: String, indexType: IndexType, indexCollectionType: IndexCollectionType): Future[IndexTask] =
    Future(client.createIndex(policy, namespace, setName, indexName, binName, indexType, indexCollectionType))

  def queryUser(policy: AdminPolicy, user: String): Future[User] =
    Future(client.queryUser(policy, user))

  def dropUser(policy: AdminPolicy, user: String): Future[Unit] =
    Future(client.dropUser(policy, user))

  //def close(): Future[Unit] = Future(client.clone()) //todo

  def queryRoles(policy: AdminPolicy): Future[util.List[Role]] =
    Future(client.queryRoles(policy))

  def grantPrivileges(policy: AdminPolicy, roleName: String, privileges: util.List[Privilege]): Future[Unit] =
    Future(client.grantPrivileges(policy, roleName, privileges))

  def register(policy: Policy, clientPath: String,
               serverPath: String, language: Language): Future[RegisterTask] =
    Future(client.register(policy, clientPath, serverPath, language))

  def register(policy: Policy, resourceLoader: ClassLoader, resourcePath: String,
               serverPath: String, language: Language): Future[RegisterTask] =
    Future(client.register(policy, resourceLoader, resourcePath, serverPath, language))

  def dropRole(policy: AdminPolicy, roleName: String): Future[Unit] =
    Future(client.dropRole(policy, roleName))

  def isConnected: Future[Boolean] = Future(client.isConnected)

  def queryRole(policy: AdminPolicy, roleName: String): Future[Role] = Future(client.queryRole(policy, roleName))

  def grantRoles(policy: AdminPolicy, user: String, roles: util.List[String]): Future[Unit] =
    Future(client.grantRoles(policy, user, roles))

  def dropIndex(policy: Policy, namespace: String, setName: String, indexName: String): Future[Unit] =
    Future(client.dropIndex(policy, namespace, setName, indexName))

  def changePassword(policy: AdminPolicy, user: String, password: String): Future[Unit] =
    Future(client.changePassword(policy, user, password))

  def queryUsers(policy: AdminPolicy): Future[util.List[User]] =
    Future(client.queryUsers(policy))

  def revokePrivileges(policy: AdminPolicy, roleName: String, privileges: util.List[Privilege]): Future[Unit] =
    Future(client.revokePrivileges(policy, roleName, privileges))

  def revokeRoles(policy: AdminPolicy, user: String, roles: util.List[String]): Future[Unit] =
    Future(client.revokeRoles(policy, user, roles))

}




