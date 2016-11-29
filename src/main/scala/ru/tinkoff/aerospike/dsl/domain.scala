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

import com.aerospike.client.listener.{ExecuteListener, RecordSequenceListener}
import com.aerospike.client.query.Statement
import com.aerospike.client.{Language, ScanCallback, Value}

/**
  * Created by danylee on 11/09/16.
  */

trait Param

case class Param1(packageName: String, functionName: String, functionArgs: List[Value],
                  statement: Option[Statement] = None, listener: Option[ExecuteListener] = None) extends Param

//structural type are expensive (reflection)

case class Param2(statement: Statement, listener: Option[RecordSequenceListener] = None) extends Param

case class Param3(namespace: String, setName: String, binNames: List[String], callback: Option[ScanCallback] = None)

case class Param4(namespace: String, setName: String, binNames: List[String], listener: Option[RecordSequenceListener])

case class Param5(code: String, serverPath: String, language: Language)