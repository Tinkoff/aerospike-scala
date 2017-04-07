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

package ru.tinkoff.aerospikeexamples.example

import com.aerospike.client.Host
import com.aerospike.client.async.{AsyncClient, AsyncClientPolicy}
import com.typesafe.config.{Config, ConfigFactory}
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikemacro.domain.DBCredentials

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * @author MarinaSigaeva
  * @since 20.10.16
  */
object AClient {

  val config: Config = ConfigFactory.load()
  val hosts: List[String] = Try(List(config.getString("ru-tinkoff-aerospike-dsl.example-host")))
    .getOrElse(throw new Exception("Add host for aerospike in application.conf file"))
  val port: Int = Try(config.getInt("ru-tinkoff-aerospike-dsl.example-port"))
    .getOrElse(throw new Exception("Add host for aerospike in application.conf file"))

  val namespace: String = Try(config.getString("ru-tinkoff-aerospike-dsl.keyWrapper-namespace"))
    .getOrElse(throw new Exception("Add namespace for aerospike in application.conf file"))
  val setName: String = Try(config.getString("ru-tinkoff-aerospike-dsl.keyWrapper-setName"))
    .getOrElse(throw new Exception("Add setName for aerospike in application.conf file"))

  def dbc = DBCredentials(namespace, setName)

  def client: AsyncClient = create(hosts)

  def create(hs: List[String]): AsyncClient =
    Try(new AsyncClient(new AsyncClientPolicy, hs.map(new Host(_, port)): _*)) match {
      case Success(c)  => c
      case Failure(th) => throw th
    }

  def spikeImpl(implicit ex: ExecutionContext) = new SpikeImpl(client)(ex)
}
