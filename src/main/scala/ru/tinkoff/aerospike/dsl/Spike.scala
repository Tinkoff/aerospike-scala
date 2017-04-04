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

import com.aerospike.client.policy._
import ru.tinkoff.aerospike.dsl.batchread.BatchReadWrapper
import ru.tinkoff.aerospikemacro.converters.KeyWrapper._
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospikescala.domain.ABin

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author MarinaSigaeva
  * @since 14.09.16
  */
trait Spike {

  //for easy types call
  def call(action: Call, any: Any = None)(
      implicit pw: Option[WritePolicy] = None,
      p: Option[Policy] = None,
      bp: Option[BatchPolicy] = None,
      qp: Option[QueryPolicy] = None,
      sp: Option[ScanPolicy] = None,
      ip: Option[InfoPolicy] = None): Future[Any]

  //for calls where you need to pass Key and Bin types (ru.tinkoff.aerospike.dsl.converters included)
  def callKB[K, B](action: CallKB, k: K, b: ABin[B])(
      implicit kC: KeyWrapper[K],
      bC: BinWrapper[B],
      pw: Option[WritePolicy] = None): Future[Unit]

  //for calls where you need to pass List[Key] type (ru.tinkoff.aerospike.dsl.converters included)
  def callKs[K](action: CallKs, ks: Array[K], any: Any = None)(
      implicit kC: KeyWrapper[K],
      bp: Option[BatchPolicy] = None,
      p: Option[Policy] = None,
      qp: Option[QueryPolicy] = None): Future[Any]

  //for calls where you need to pass Key type (ru.tinkoff.aerospike.dsl.converters included)
  def callK[K](action: CallK, k: K, any: Any = None)(
      implicit kC: KeyWrapper[K],
      p: Option[Policy] = None,
      pw: Option[WritePolicy] = None,
      bp: Option[BatchPolicy] = None,
      sp: Option[ScanPolicy] = None,
      ip: Option[InfoPolicy] = None): Future[Any]

  def getByKey[K, B](k: K, bs: List[String] = Nil)(
      implicit kC: KeyWrapper[K],
      bC: BinWrapper[B],
      ec: ExecutionContext,
      optP: Option[Policy] = None): Future[Option[(Map[String, Option[B]], Int, Int)]]

  def getByKeys[K, B](ks: Array[K], bs: List[String] = Nil)(
      implicit kC: KeyWrapper[K],
      bC: BinWrapper[B],
      ec: ExecutionContext,
      optBP: Option[BatchPolicy] = None): Future[List[Option[(Map[String, Option[B]], Int, Int)]]]

  def getByKeysWithListener[K, L](ks: Array[K],
                                  listener: L,
                                  bs: List[String] = Nil)(
      implicit kC: KeyWrapper[K],
      optBP: Option[BatchPolicy] = None): Future[Unit]

  //note, if you will not change namespace, setName parameters in BatchReadWrapper - default values from application.conf will be used
  def getByKeysWithBatchListener[L](kws: List[BatchReadWrapper],
                                    listener: Option[L] = None)(
      implicit optBP: Option[BatchPolicy] = None): Future[Unit]

  def deleteK[K](k: K)(implicit kC: KeyWrapper[K],
                       pw: Option[WritePolicy] = None,
                       e: ExecutionContext): Future[Boolean]
}
