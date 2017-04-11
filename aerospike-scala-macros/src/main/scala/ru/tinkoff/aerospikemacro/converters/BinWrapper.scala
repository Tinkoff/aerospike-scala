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

package ru.tinkoff.aerospikemacro.converters

import ru.tinkoff.aerospikescala.domain.{ByteSegment, MBin, SingleBin}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import Utils._

/**
  * @author MarinaSigaeva
  * @since 08.09.16
  */
trait BinWrapper[BT] {

  import com.aerospike.client.Value._
  import com.aerospike.client.{Bin, Record, Value}
  import shapeless.HList.hlistOps
  import shapeless.{HList, _}

  import scala.collection.JavaConverters._
  import scala.collection.immutable.{List, Map}
  import scala.reflect.runtime.universe._
  import scala.util.{Failure, Success}

  type One       = (String, BT)
  type Singleton = SingleBin[BT]
  type Many      = Map[String, BT]
  type Multi     = MBin[BT]
  type Out       = (Map[String, Option[BT]], Int, Int)

  def apply(many: Many): List[Bin] =
    many.view.flatMap(one => scala.util.Try(apply(one)).toOption).toList

  def apply(many: Multi): List[Bin] = many.asOne.view.map(apply).toList

  def apply(one: One): Bin =
    if (one._1.length > 14)
      throwE("Current limit for bin name is 14 characters")
    else gen(one)

  def apply(one: Singleton): Bin = apply((one.name, one.value))

  def apply(r: Record): Out = {
    val outValue: Map[String, Option[BT]] = {
      r.bins.asScala
        .collect {
          case (name, bt: Any) => name -> fetch(bt)
        }
        .iterator
        .toMap
    }
    if (outValue.values.isEmpty && !r.bins.isEmpty)
      throw new ClassCastException(
        s"Failed to cast ${weakTypeOf[BT]}. Please, implement fetch function in BinWrapper"
      )
    else (outValue, r.generation, r.expiration)
  }

  /**
  saving as BlobValue, GeoJSONValue, ValueArray or NullValue not implemented here
  Your case classes will be saved as Map[String, Any] in com.aerospike.client.MapValue<String, Object>.
  If you want another format just override  toValue function
    */
  def toValue(v: BT): Value = v match {
    case h: HList =>
      val m = fromHList(h, 0, h.runtimeLength - 1)
      new MapValue(m.asJava)
    case ByteSegment(bytes, offset, length) =>
      new ByteSegmentValue(bytes, offset, length)
    case b: Int                  => new IntegerValue(b)
    case b: String               => new StringValue(b)
    case b: Short                => new IntegerValue(b)
    case b: Char                 => new StringValue(b.toString)
    case b: Byte                 => new IntegerValue(b)
    case b: Long                 => new LongValue(b)
    case b: Boolean              => new BooleanValue(b)
    case b: Float                => new FloatValue(b)
    case b: Double               => new DoubleValue(b)
    case b: Array[Byte]          => new BytesValue(b)
    case jl: java.util.List[_]   => new ListValue(jl)
    case s: List[_]              => new ListValue(s.asJava)
    case a: Array[_]             => new ListValue(a.toList.asJava)
    case jm: java.util.Map[_, _] => new MapValue(jm)
    case m: Map[_, _]            => new MapValue(m.asJava)
    case t: Any with Product if isTuple(t) =>
      new MapValue(tupleMapped(t).asJava)
    case yourCaseClass =>
      scala.util.Try(defaultToValue(yourCaseClass)) match {
        case Success(m) => new MapValue(m.asJava)
        case Failure(_) =>
          throwE(
            s"You need to write your own function toValue(v: ${v.getClass}): " +
              "com.aerospike.client.Value function in BinWrapper implicit"
          )
      }
  }

  def fetch(any: Any): Option[BT] = scala.util.Try(any.asInstanceOf[BT]).toOption

  def gen(b: One): Bin = new Bin(b._1, toValue(b._2))

  def throwE(msg: String) = throw new IllegalArgumentException(msg)

  def throwClassCast(tpe: String) =
    throw new ClassCastException(
      s"Failed to cast $tpe. Please, implement fetch function in BinWrapper"
    )

  def toKVmap[K, V](
      any: Any,
      getView: String => Array[String] = plain
  )(implicit k: String => K, v: String => V): Map[K, V] = any match {
    case a: Value =>
      val objString = a.getObject.toString
      val anyView   = getView(objString).view
      (for {
        elem <- anyView
        kvs = elem.split("=") if kvs.length > 1
      } yield k(kvs(0)) -> v(kvs(1))).toMap
    case _ => Map.empty
  }

  def toLs[T](s: String)(implicit to: String => T): List[T] =
    s.view(5, s.length).mkString.split(", ").view.map(to).toList

  def plain(s: String): Array[String] =
    s.view(1, s.length - 1).mkString.split(", ")

  def coll(s: String): Array[String] = {
    val all = s.view(1, s.length - 1).mkString.split("\\), ", s.length).view
    all.dropRight(1).toArray ++ Array(all.last.dropRight(1))
  }
}

object BinWrapper {

  implicit def materializeBinWrapper[T]: BinWrapper[T] =
    macro materializeBinWrapperImpl[T]

  def materializeBinWrapperImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[BinWrapper[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val out   = weakTypeOf[(Map[String, Option[T]], Int, Int)]
    val tpeSt = q"${tpe.toString}"

    println("type " + tpe)
/*    def castTuple[T](elems: Map[Any, Any], types: List[String])(implicit tf: Tuplify[T]): Option[T] = {
      val casted = types.indices.map(i => cast(elems(i.toString), types(i))).toList
      casted.length match {
        case l if l > 0 && l < 23 => scala.util.Try {
          val k = tf.toTuple(casted)
          println("buildTupleResult " + k)
          k
        }.toOption
        case _ => None
      }
    }*/

    def mp(add: Tree) =
      q"""override def fetch(any: Any): Option[$tpe] = any match {
            case v: $tpe => Option(v)
            case any: Any => scala.util.Try{$add}.toOption
            case oth => None
          }
       """

    def tupleFetch(ts: List[String]) =
      q"""override def fetch(any: Any): Option[$tpe] =
            Value.getFromRecordObject(any) match {
              case m: MapValue => m.getObject match {
                case ms: java.util.Map[Any @unchecked, Any @unchecked] =>
                  println("tuple from map " + ms.asScala.iterator.toMap)
                  val elems = ms.asScala.iterator.toMap
                  val types = $ts
                  val casted = types.indices.map(i => cast(elems(i.toString), types(i))).toList
                  val sk = Tuplify.m(casted.length)
                  scala.util.Try(sk(casted).asInstanceOf[$tpe]).toOption

                case _ => None
              }
              case _ => None
            }
        """

    val mh =
      q"""override def fetch(any: Any): Option[$tpe] = Value.getFromRecordObject(any) match {
            case m: MapValue => m.getObject match {
              case ms: java.util.Map[Any @unchecked, Any @unchecked] =>
                val newList = castHListElements(ms.asScala.values.toList, $tpeSt)
                newList.toHList[$tpe]
              case _ => None
            }
            case _ => None
          }"""

    def typedList(pType: Tree): Tree =
      q"""override def fetch(any: Any): Option[$tpe] =
          Value.getFromRecordObject(any) match {
            case lv: ListValue => lv.getObject match {
                  case ls: java.util.List[$pType @unchecked] => Some(ls.asScala.toList)
                  case _                                     => None
                }
            case _ => None
          }
        """

    def typedArray(pType: Tree): Tree =
      q"""override def fetch(any: Any): Option[$tpe] =
          Value.getFromRecordObject(any) match {
            case lv: ListValue => lv.getObject match {
                  case ls: java.util.List[$pType @unchecked] => Some(ls.asScala.toArray)
                  case _                                     => None
                }
            case _ => None
          }
        """

    def streamedArray(pType: Tree, to: Tree): Tree =
      q"""override def fetch(any: Any): Option[$tpe] =
          Value.getFromRecordObject(any) match {
            case lv: ListValue => lv.getObject match {
                  case ls: java.util.List[$pType @unchecked] => Some(ls.asScala.map($to).toArray)
                  case _                                     => None
                }
            case _ => None
          }
        """

    def typedMap(k: Tree, v: Tree): Tree =
      q""" override def fetch(any: Any): Option[$tpe] =
           Value.getFromRecordObject(any) match {
             case m: MapValue => m.getObject match {
                 case ms: java.util.Map[$k @unchecked, $v @unchecked] => Some(ms.asScala.iterator.toMap)
               case _                                     => None
                }
            case _ => None
          }
        """

    def tupleArity(tpe: Type): Int = {
      val rex = "Tuple(\\d{1,2})".r
      tpe.typeSymbol.name.encodedName.toString match {
        case rex(n) if n.toInt > 1 && n.toInt < 23 => n.toInt
        case _                                     => 0
      }
    }


    def typedPlain(pType: Tree, cast: Tree): Tree =
      q"""override def fetch(any: Any): Option[$tpe] = any match {
            case v: $pType => Option($cast)
            case _ => None
          }"""

    def floatOrDouble(pType: Tree = q"""v""", backup: Tree = q"""java.lang.Double.longBitsToDouble(v)"""): Tree =
      q"""override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.lang.Double => Option($pType)
              case v: java.lang.Long   =>
                Option($backup)
              case _ => None
            }
        """

    val fetchValue = tpe match {
      case t if t.toString.contains("HNil") || t.toString.contains("HList") => mh
      case t if t =:= weakTypeOf[String] =>
        q"""override def fetch(any: Any): Option[$tpe] =
           Try(Value.getFromRecordObject(any).getObject.toString).toOption
          """
      case t if t =:= weakTypeOf[Boolean] => typedPlain(tq"java.lang.Long", q"v == 1")
      // ToDo Choose between String and Long. Take storage overhead into account. Implement test.
      case t if t =:= weakTypeOf[Char] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
              case v: String => v.toString.toCharArray.headOption
              case _ => None
            } """
      // ToDo Use strict checking of Value.UseDoubleType. Implement test.
      case t if t =:= weakTypeOf[Float] => floatOrDouble(q"""v.toFloat""", q"""java.lang.Double.longBitsToDouble(v).toFloat""")
      // ToDo Use strict checking of Value.UseDoubleType. Implement test.
      case t if t =:= weakTypeOf[Double] => floatOrDouble()
      case t if t =:= weakTypeOf[Int] => typedPlain(tq"java.lang.Long", q"v.toInt")
      case t if t =:= weakTypeOf[Short] => typedPlain(tq"java.lang.Long", q"v.toShort")
      case t if t =:= weakTypeOf[Byte] => typedPlain(tq"java.lang.Long", q"v.toByte")
      case t if t =:= weakTypeOf[List[String]] => typedList(tq"String")
      case t if t =:= weakTypeOf[List[Int]] => typedList(tq"Int")
      case t if t =:= weakTypeOf[List[Long]] => typedList(tq"Long")
      case t if t =:= weakTypeOf[List[Float]] => typedList(tq"Float")
      case t if t =:= weakTypeOf[List[Double]] => typedList(tq"Double")
      case t if t =:= weakTypeOf[List[Boolean]] => typedList(tq"Boolean")
      case t if t =:= weakTypeOf[Array[String]] => typedArray(tq"String")
      case t if t =:= weakTypeOf[Array[Int]] => streamedArray(tq"Long", q"_.toInt")
      case t if t =:= weakTypeOf[Array[Long]] => typedArray(tq"Long")
      case t if t =:= weakTypeOf[Array[Float]] => streamedArray(tq"Double", q"_.toFloat")
      case t if t =:= weakTypeOf[Array[Double]] => typedArray(tq"Double")
      case t if t =:= weakTypeOf[Array[Boolean]] => typedArray(tq"Boolean")
      case t if t =:= weakTypeOf[Map[Int, String]] => typedMap(tq"Int", tq"String")
      case t if t =:= weakTypeOf[Map[String, String]] => typedMap(tq"String", tq"String")
      case t if t =:= weakTypeOf[Map[String, Int]] => typedMap(tq"String", tq"Int")
      case t if t =:= weakTypeOf[Map[String, Long]] => typedMap(tq"String", tq"Long")
      case t if t =:= weakTypeOf[Map[String, Float]] => typedMap(tq"String", tq"Float")
      case t if t =:= weakTypeOf[Map[String, Double]] => typedMap(tq"String", tq"Double")
      case t if t =:= weakTypeOf[Map[String, List[Int]]] => mp(q"""toKVmap[String, List[Int]](any, coll)(_.toString, toLs(_)(_.toInt))""")
      case t if t =:= weakTypeOf[Map[String, List[String]]] => mp(q"""toKVmap[String, List[String]](any, coll)(_.toString, toLs(_))""")
      case t if t =:= weakTypeOf[Map[String, Any]] => typedMap(tq"String", tq"Any")
      case t if tupleArity(t) != 0 =>
        val tplArity = tupleArity(t)
        println("IM IN TUPLE " + t)
        if (tplArity > 0 && tplArity < 23) tupleFetch(t.typeArgs.map(_.toString))
        else q"""None"""
      case _ => q""""""
    }

    c.Expr[BinWrapper[T]] {
      q"""

      import java.util.{List => JList, Map => JMap}
      import com.aerospike.client.{Bin, Record, Value}
      import com.aerospike.client.Value._
      import scala.collection.JavaConverters._
      import scala.collection.mutable.{Seq => mSeq}
      import scala.language.experimental.macros
      import shapeless.{HList, _}
      import shapeless.HList.hlistOps
      import syntax.std.traversable._
      import scala.collection.immutable.ListMap
      import ru.tinkoff.aerospikemacro.cast.Caster._
      import ru.tinkoff.aerospikemacro.cast.Tuplify._
      import ru.tinkoff.aerospikemacro.cast.Tuplify
      import ru.tinkoff.aerospikemacro.converters._
      import com.aerospike.client.Value
      import scala.util.Try

      new BinWrapper[$tpe] {
        override def apply(r: Record): $out = {
          val outValue: collection.immutable.Map[String, Option[$tpe]] = {
              r.bins.asScala.collect {
              case (name, bt: Any) =>
                val res = fetch(bt)
                if (res.isEmpty && !r.bins.isEmpty) throwClassCast($tpeSt) else name -> res
            }.iterator.toMap
          }

          (outValue, r.generation, r.expiration)
        }
        $fetchValue
      }

    """
    }
  }
}
