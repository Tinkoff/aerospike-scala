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

import com.aerospike.client._
import com.aerospike.client.async._
import com.aerospike.client.listener._
import com.aerospike.client.policy._
import com.aerospike.client.query.Statement
//import ru.tinkoff.aerospike.dsl.batchread.BatchReadWrapper
import ru.tinkoff.aerospikemacro.converters.{BinWrapper, KeyWrapper}
import ru.tinkoff.aerospike.dsl.errors.AerospikeDSLError
import ru.tinkoff.aerospikescala.domain.{ABin, MBin, SingleBin}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author MarinaSigaeva 
  * @since 08.09.16
  */
class SpikeImpl(spikeClient: IAsyncClient)(implicit val ec: ExecutionContext) extends Spike with MainProvider
  with GetProvider with AdminProvider with CollectionsProvider with HeaderProvider with NodeProvider with PolicyProvider {

  val client: IAsyncClient = spikeClient

  def call(action: Call, any: Any = None)(implicit pw: Option[WritePolicy] = None, p: Option[Policy] = None,
                                          bp: Option[BatchPolicy] = None, qp: Option[QueryPolicy] = None,
                                          sp: Option[ScanPolicy] = None, ip: Option[InfoPolicy] = None): Future[Any] = {
    import Call._
    any match {
      case Param1(pkg, fName, args, Some(stmnt), listener) if (action is Execute) && listener.isEmpty => execute(pw.getOrElse(new WritePolicy), stmnt, pkg, fName, args: _*)
      case Param2(statement, Some(listener)) if action is Query => query(qp.getOrElse(new QueryPolicy), listener, statement)
      case Param2(statement, _) if action is Query => query(qp.getOrElse(new QueryPolicy), statement)
      case Param1(pkg, fName, args, Some(stmnt), listener) if (action is QueryAggregate) && listener.isEmpty =>
        queryAggregate(qp.getOrElse(new QueryPolicy), stmnt, pkg, fName, args: _*)
      case statement: Statement if action is QueryAggregate => queryAggregate(qp.getOrElse(new QueryPolicy), statement)
      case Param4(namespace: String, setName: String, binNames: List[String], Some(listener: RecordSequenceListener)) if action is ScanAll =>
        scan(namespace, setName, binNames, Option(listener))
      case Param3(namespace: String, setName: String, binNames: List[String], Some(callback: ScanCallback)) if action is ScanAll =>
        scan(namespace, setName, binNames, callback = Option(callback))
      case serverPath: String if action is RemoveUdf => removeUdf(ip.getOrElse(new InfoPolicy), serverPath)
      case Param5(code: String, serverPath: String, language: Language) if action is RegisterUdfString =>
        registerUdfString(p.getOrElse(new Policy), code, serverPath, language)
      case _ => throw AerospikeDSLError(s"Unsupported type ${any.getClass} or action $action is not available for this type. " +
        s"You can use: ${Call.all}")
    }
  }

  def callKs[K](action: CallKs, ks: Array[K], any: Any = None)(implicit kC: KeyWrapper[K], bp: Option[BatchPolicy] = None,
                                                               p: Option[Policy] = None, qp: Option[QueryPolicy] = None): Future[Any] = {
    import CallKs._
    any match {
      case eal: ExistsArrayListener if action is Exists => existsByKeys(ks, Option(eal))
      case esl: ExistsSequenceListener if action is Exists => existsByKeys(ks, Option(esl))
      case _ if action is Exists => existsByKeys(ks)
      case _ => throw AerospikeDSLError(s"Unsupported type ${any.getClass} or action $action is not available for this type. " +
        s"You can use: Exists")
    }
  }

  def callK[K](action: CallK, k: K, any: Any = None)(implicit kC: KeyWrapper[K], p: Option[Policy] = None, pw: Option[WritePolicy] = None,
                                                     bp: Option[BatchPolicy] = None,
                                                     sp: Option[ScanPolicy] = None, ip: Option[InfoPolicy] = None): Future[Any] = {
    import CallK._
    any match {
      case ops: List[Operation] if action is Operate => operate(pw.getOrElse(new WritePolicy), kC(k), ops: _*)
      case (ops: List[Operation], listener: RecordListener) if action is Operate => operate(pw.getOrElse(new WritePolicy), listener, kC(k), ops: _*)
      case listener: DeleteListener if action is Delete => deleteByKey(k, Option(listener))
      case _ if action is Delete => deleteByKey(k)
      case listener: WriteListener if action is Touch => touchByKey(k, Option(listener))
      case _ if action is Touch => touchByKey(k)
      case Param1(pkg, fName, args, stmnt, listener)
        if (action is Execute) && stmnt.isEmpty => execByKey(k, pkg, fName, args, listener)
      case Param1(pkg, fName, args, stmnt, listener)
        if (action is Execute) && stmnt.isEmpty && listener.isEmpty => execByKey(k, pkg, fName, args)
      case listener: ExistsListener if action is Exists => existsByKey(k, Option(listener))(kC, p)
      case _ if action is Exists => existsByKey(k)(kC, p)
      case _ => throw AerospikeDSLError(s"Unsupported type ${any.getClass} or action $action is not available for this type. " +
        s"You can use: ${CallK.all}")

    }
  }

  def callKB[K, B](action: CallKB, k: K, b: ABin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], pw: Option[WritePolicy] = None): Future[Unit] = {
    import CallKB._
    b match {
      case one: SingleBin[B] if action is Put => putOne(k, one)
      case one: SingleBin[B] if action is Append => appendOne(k, one)
      case one: SingleBin[B] if action is Prepend => prependOne(k, one)
      case one: SingleBin[B] if action is Add => addOne(k, one)
      case oneM: MBin[B] if action is Put => putMany(k, oneM)
      case oneM: MBin[B] if action is Append => appendMany(k, oneM)
      case oneM: MBin[B] if action is Prepend => prependMany(k, oneM)
      case oneM: MBin[B] if action is Add => addMany(k, oneM)
      case _ => throw AerospikeDSLError(s"Unsupported type ${b.getClass} or action $action is not available for this type. " +
        s"You can use: ${CallKB.all}")
    }
  }

  def deleteK[K](k: K)(implicit kC: KeyWrapper[K], pw: Option[WritePolicy] = None, e: ExecutionContext): Future[Boolean] = {
    val writePolicy = pw.getOrElse(new WritePolicy)
    delete(writePolicy, kC(k))
  }

  protected def putOne[K, B](k: K, b: SingleBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    put(writePolicy, kC(k), bC.apply(b))
  }

  protected def putMany[K, B](k: K, bs: MBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    put(writePolicy, kC(k), bC.apply(bs): _*)
  }

  protected def appendOne[K, B](k: K, b: SingleBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    append(writePolicy, kC(k), bC.apply(b))
  }

  protected def appendMany[K, B](k: K, bs: MBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    append(writePolicy, kC(k), bC.apply(bs): _*)
  }

  protected def prependOne[K, B](k: K, b: SingleBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    prepend(writePolicy, kC(k), bC.apply(b))
  }

  protected def prependMany[K, B](k: K, bs: MBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    prepend(writePolicy, kC(k), bC.apply(bs): _*)
  }

  protected def addOne[K, B](k: K, b: SingleBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    add(writePolicy, kC(k), bC.apply(b))
  }

  protected def addMany[K, B](k: K, bs: MBin[B])(implicit kC: KeyWrapper[K], bC: BinWrapper[B], optWP: Option[WritePolicy] = None): Future[Unit] = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    add(writePolicy, kC(k), bC.apply(bs): _*)
  }

  protected def deleteByKey[K](k: K, optListener: Option[DeleteListener] = None)(implicit kC: KeyWrapper[K], optWP: Option[WritePolicy] = None) = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    optListener.map(listener => delete(writePolicy, listener, kC(k))).getOrElse(delete(writePolicy, kC(k)))
  }

  protected def touchByKey[K](k: K, optListener: Option[WriteListener] = None)(implicit kC: KeyWrapper[K], optWP: Option[WritePolicy] = None) = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    optListener.map(listener => touch(writePolicy, listener, kC(k))).getOrElse(touch(writePolicy, kC(k)))
  }

  protected def execByKey[K](k: K, pkg: String, fName: String, args: List[Value], optListener: Option[ExecuteListener] = None)
                            (implicit kC: KeyWrapper[K], optWP: Option[WritePolicy] = None) = {
    val writePolicy = optWP.getOrElse(new WritePolicy)
    optListener.map(listener => execute(writePolicy, listener, kC(k), pkg, fName, args: _*))
      .getOrElse(execute(writePolicy, kC(k), pkg, fName, args: _*))
  }

  protected def existsByKeys[K, L](ks: Array[K], optListener: Option[L] = None)(implicit kC: KeyWrapper[K], bp: Option[BatchPolicy] = None) = {
    val policy = bp.getOrElse(new BatchPolicy)
    val keys = ks.view.map(k => kC(k)).toArray
    optListener match {
      case Some(eal: ExistsArrayListener) => exists(policy, eal, keys)
      case Some(esl: ExistsSequenceListener) => exists(policy, esl, keys)
      case _ => exists(policy, keys)
    }
  }

  protected def existsByKey[K](k: K, optListener: Option[ExistsListener] = None)(implicit kC: KeyWrapper[K], p: Option[Policy] = None) = {
    val policy = p.getOrElse(new Policy)
    optListener.map(el => exists(policy, el, kC(k))).getOrElse(exists(policy, kC(k)))
  }

  protected def scan(namespace: String, setName: String, binNames: List[String], listener: Option[RecordSequenceListener] = None,
                     callback: Option[ScanCallback] = None)(implicit sp: Option[ScanPolicy] = None) = {
    val policy = sp.getOrElse(new ScanPolicy)
    (listener, callback) match {
      case (Some(rl: RecordSequenceListener), _) => scanAll(policy, rl, namespace, setName, binNames: _*)
      case (None, Some(cb)) => scanAll(policy, namespace, setName, cb, binNames: _*)
      case _ => throw AerospikeDSLError("Not supported scanAll type")
    }
  }

  def getByKey[K, B](k: K, bs: List[String] = Nil)(implicit kC: KeyWrapper[K], bC: BinWrapper[B],
                                                   ec: ExecutionContext, optP: Option[Policy] = None): Future[Option[(Map[String, Option[B]], Int, Int)]] = {
    val policy = optP.getOrElse(new Policy)
    if (bs.isEmpty) Future(get(policy, kC(k)).map(bC(_)))(ec) else Future(get(policy, kC(k), bs: _*).map(record => bC(record)))(ec)
  }

  def getByKeyWithListener[K](k: K, listener: RecordListener, bs: List[String] = Nil)(implicit kC: KeyWrapper[K], optP: Option[Policy] = None): Future[Unit] = {
    val policy = optP.getOrElse(new Policy)
    if (bs.isEmpty) get(policy, listener, kC(k)) else get(policy, listener, kC(k), bs: _*)
  }

  def getByKeys[K, B](ks: Array[K], bs: List[String] = Nil)(implicit kC: KeyWrapper[K], bC: BinWrapper[B],
                                                            ec: ExecutionContext, optBP: Option[BatchPolicy] = None): Future[List[Option[(Map[String, Option[B]], Int, Int)]]] = {
    val policy = optBP.getOrElse(new BatchPolicy)
    val keys = ks.view.map(k => kC(k)).toArray
    if (bs.isEmpty) Future(get(policy, keys).map(_.map(record => bC(record))).toList)(ec)
    else Future(get(policy, keys, bs: _*).map(_.map(record => bC(record))).toList)(ec)
  }

  def getByKeysWithListener[K, L](ks: Array[K], listener: L, bs: List[String] = Nil)(implicit kC: KeyWrapper[K], optBP: Option[BatchPolicy] = None): Future[Unit] = {
    val policy = optBP.getOrElse(new BatchPolicy)
    val keys = ks.view.map(k => kC(k)).toArray

    listener match {
      case l: RecordArrayListener => if (bs.isEmpty) get(policy, l, keys) else get(policy, l, keys, bs: _*)
      case l: RecordSequenceListener => if (bs.isEmpty) get(policy, l, keys) else get(policy, l, keys, bs: _*)
      case _ => throw AerospikeDSLError(s"Unsupported listener type $listener")
    }
  }

/*  def getByKeysWithBatchListener[L](kws: List[BatchReadWrapper], listener: Option[L] = None)(implicit optBP: Option[BatchPolicy] = None): Future[Unit] = {
    val policy = optBP.getOrElse(new BatchPolicy)
    val records: java.util.List[BatchRead] = kws.view.map(e => e.apply).toList

    listener match {
      case Some(l: BatchListListener) => get(policy, l, records)
      case Some(ls: BatchSequenceListener) => getS(policy, ls, records)
      case None => get(policy, records)
      case _ => throw AerospikeDSLError(s"Unsupported listener type $listener")
    }
  }*/

}

