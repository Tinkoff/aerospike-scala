/*
 * Copyright (c) 2017 Tinkoff
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

import com.aerospike.client.policy.WritePolicy
import com.trueaccord.lenses.Updatable
import com.trueaccord.scalapb.{GeneratedMessage, Message}
import ru.tinkoff.aerospike.dsl.CallKB
import ru.tinkoff.aerospikemacro.converters.KeyWrapper
import ru.tinkoff.aerospikescala.domain.SingleBin

import scala.concurrent.{ExecutionContext, Future}
import ru.tinkoff.aerospikeproto.wrapper.ProtoBinWrapper
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author MarinaSigaeva
  * @since 23.03.17
  */
class ProtoScheme {

  val client = AClient.spikeImpl

  def put[K, I <: GeneratedMessage with Message[I] with Updatable[I], R <: ProtoBinWrapper[I]]
  (k: K, bin: SingleBin[I])(implicit kw: KeyWrapper[K],
                            bw: R, e: ExecutionContext,
                            pw: Option[WritePolicy] = None): Future[Unit] = {
    client.callKB[K, I](CallKB.Put, k, bin)(kw, bw, pw)
  }

  def absGet[K, I <: GeneratedMessage with Message[I] with Updatable[I], R <: ProtoBinWrapper[I]]
  (k: K)(implicit kw: KeyWrapper[K], bw: R, e: ExecutionContext,
         pw: Option[WritePolicy] = None): Future[Map[String, Option[I]]] = {
    client.getByKey[K, I](k)(kw, bw, e, pw).map(r => r.map(_._1).getOrElse(throw new Exception("No data found")))
  }

  def get[I <: GeneratedMessage with Message[I] with Updatable[I]]
  (k: String)(implicit kw: KeyWrapper[String], bw: ProtoBinWrapper[I],
              e: ExecutionContext, pw: Option[WritePolicy] = None): Future[Map[String, Option[I]]] =
    absGet[String, I, ProtoBinWrapper[I]](k)(kw, bw, e, pw)
}
