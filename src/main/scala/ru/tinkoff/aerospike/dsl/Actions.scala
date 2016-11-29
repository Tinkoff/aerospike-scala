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

/**
  * @author MarinaSigaeva 
  * @since 08.09.16
  */

trait Call {
  def is(other: Call): Boolean = this.toString.equalsIgnoreCase(other.toString)
}

object Call extends Call {
  val all = List(Execute, Query, QueryAggregate, ScanAll, RemoveUdf, RegisterUdfString).mkString(", ")

  case object Execute extends Call

  case object Query extends Call

  case object QueryAggregate extends Call

  case object ScanAll extends Call

  case object RemoveUdf extends Call

  case object RegisterUdfString extends Call

}

trait CallKs {
  def is(other: CallKs): Boolean = this.toString.equalsIgnoreCase(other.toString)
}

object CallKs extends CallKs {

  case object Exists extends CallKs

}

trait CallKB {
  def is(other: CallKB): Boolean = this.toString.equalsIgnoreCase(other.toString)
}

object CallKB extends CallKB {
  val all = List(Append, Put, Prepend, Add).mkString(", ")

  case object Put extends CallKB

  case object Append extends CallKB

  case object Prepend extends CallKB

  case object Add extends CallKB

}

trait CallK {
  def is(other: CallK): Boolean = this.toString.equalsIgnoreCase(other.toString)
}

object CallK extends CallK {
  val all = List(Operate, Delete, Touch, Execute, Exists).mkString(", ")

  case object Operate extends CallK

  case object Delete extends CallK

  case object Touch extends CallK

  case object Exists extends CallK

  case object Execute extends CallK

}