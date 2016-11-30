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


/**
  * @author MarinaSigaeva 
  * @since 08.09.16
  */
trait BinWrapper[BT] {

  import com.aerospike.client.Value._
  import com.aerospike.client.{Bin, Record, Value}
  import shapeless.HList.hlistOps
  import shapeless.{HList, _}

  import scala.collection.JavaConversions._
  import scala.collection.immutable.{List, ListMap, Map}
  import scala.reflect.ClassTag
  import scala.reflect.runtime.universe._
  import scala.util.{Failure, Success}

  type One = (String, BT)
  type Singleton = SingleBin[BT]
  type Many = Map[String, BT]
  type Multi = MBin[BT]
  type Out = (Map[String, Option[BT]], Int, Int)

  val comma = ","

  def apply(many: Many): List[Bin] = many.view.flatMap(one => scala.util.Try(apply(one)).toOption).toList

  def apply(many: Multi): List[Bin] = many.asOne.view.map(apply).toList

  def apply(one: One): Bin =
    if (one._1.length > 14) throwE("Current limit for bean name is 14 characters")
    else gen(one)

  def apply(one: Singleton): Bin = apply((one.name, one.value))

  /*
  saving as BlobValue, GeoJSONValue, ValueArray or NullValue not implemented here
  Your case classes will be saved as Map[String, Any] in com.aerospike.client.MapValue<String, Object>.
  If you want another format just override  toValue function
  */

  def toValue(v: BT): Value = v match {
    case h: HList => val m = fromHList(h, 0, h.runtimeLength - 1)
      new MapValue(m)
    case ByteSegment(bytes, offset, length) => new ByteSegmentValue(bytes, offset, length)
    case b: Int => new IntegerValue(b)
    case b: String => new StringValue(b)
    case b: Short => new IntegerValue(b)
    case b: Char => new StringValue(b.toString)
    case b: Byte => new IntegerValue(b)
    case b: Long => new LongValue(b)
    case b: Boolean => new BooleanValue(b)
    case b: Float => new FloatValue(b)
    case b: Double => new DoubleValue(b)
    case b: Array[Byte] => new BytesValue(b)
    case jl: java.util.List[_] => new ListValue(jl)
    case s: List[_] => new ListValue(s)
    case a: Array[_] => new ListValue(a.toList)
    case jm: java.util.Map[_, _] => new MapValue(jm)
    case m: Map[_, _] => new MapValue(m)
    case t: Any with Product if isTuple(t) => new MapValue(tupleMapped(t))
    case yourCaseClass => scala.util.Try(defaultToValue(yourCaseClass)) match {
      case Success(m) => new MapValue(m)
      case Failure(f) => throwE(s"You need to write your own function toValue(v: ${v.getClass}): " +
        "com.aerospike.client.Value function in BinWrapper implicit")
    }
  }

  def typed[T](x: T)(implicit tag: WeakTypeTag[T]): T = x.asInstanceOf[T]

  def tupleMapped[TPL <: Any with Product](tpl: TPL): Map[String, Any] = {
    val i = tpl.productArity
    val m = optTuple(tpl).map(mapify(i, _)).getOrElse(Map.empty)
    ListMap(m.toSeq.sortBy(_._1): _*).view.map {
      case (k, v) => k.toString -> v
    }.toMap
  }

  def mapify[H <: Any with Product](i: Int, t: H): Map[Int, Any] = {
    (for (e <- 0 until i) yield (e, t.productElement(e))) (collection.breakOut)
  }

  def fromHList[L <: HList](hList: L, i: Int, maxIndex: Int): Map[String, Any] = {
    val h0 = Map(i.toString -> typed(hList.productElement(0)))
    hList match {
      case head :: tail if i < maxIndex => h0 ++ fromHList(tail, i + 1, maxIndex)
      case _ => h0
    }
  }

  def defaultToValue[T](x: T): Map[String, Any] = {
    val clazz = weakTypeOf[T].getClass
    val classTag = ClassTag[T](clazz)
    val rm = scala.reflect.runtime.currentMirror
    val accessors = rm.classSymbol(x.getClass).toType.decls.sorted.view.collect {
      case m: MethodSymbol if m.isGetter && m.isPublic => m
    }
    val instanceMirror = rm.reflect(x)(classTag)
    (for (acc <- accessors.view) yield {
      acc.name.toString -> instanceMirror.reflectMethod(acc).apply()
    }).toMap
  }

  def apply(r: Record): Out = {
    val outValue: Map[String, Option[BT]] = {
      val jMap = r.bins.view collect {
        case (name, bt: Any) => name -> fetch(bt)
      }
      jMap.toMap
    }
    if (outValue.values.isEmpty && r.bins.nonEmpty) throw new ClassCastException(
      s"Failed to cast ${weakTypeOf[BT]}. Please, implement fetch function in BinWrapper")
    else (outValue, r.generation, r.expiration)
  }

  def fetch(any: Any): Option[BT] = scala.util.Try(any.asInstanceOf[BT]).toOption

  def gen(b: One): Bin = new Bin(b._1, toValue(b._2))

  def throwE(msg: String) = throw new IllegalArgumentException(msg)

  def throwClassCast(tpe: String) = throw new ClassCastException(s"Failed to cast $tpe. Please, implement fetch function in BinWrapper")

  def fetchTuple(any: Any): Option[BT] = {
    any match {
      case m: java.util.Map[String, Any] => toTuple(m.toMap)
      case _ => None
    }
  }

  def toTuple(m: Map[String, Any]): Option[BT] = None

  def toKVmap[K, V](any: Any, getView: String => Array[String] = plain)
                   (implicit k: String => K, v: String => V): Map[K, V] = any match {
    case a: Value =>
      val objString = a.getObject.toString
      val anyView = getView(objString).view
      (for {elem <- anyView
            kvs = elem.split("=") if kvs.length > 1
      } yield k(kvs(0)) -> v(kvs(1))).toMap
    case oth => Map.empty
  }

  def toLs[T](s: String)(implicit to: String => T): List[T] = s.view(5, s.length).mkString.split(", ").view.map(to).toList

  def plain(s: String): Array[String] = s.view(1, s.length - 1).mkString.split(", ")

  def coll(s: String): Array[String] = {
    val all = s.view(1, s.length - 1).mkString.split("\\), ", s.length).view
    all.dropRight(1).toArray ++ Array(all.last.dropRight(1))
  }

  def isTuple[T](x: T) = x.getClass.getSimpleName.contains("Tuple")

  def optTuple[TPL <: Any with Product](tpl: TPL) = tpl match {
    case t2: Tuple2[_, _] => Option(t2)
    case t3: Tuple3[_, _, _] => Option(t3)
    case t4: Tuple4[_, _, _, _] => Option(t4)
    case t5: Tuple5[_, _, _, _, _] => Option(t5)
    case t6: Tuple6[_, _, _, _, _, _] => Option(t6)
    case t7: Tuple7[_, _, _, _, _, _, _] => Option(t7)
    case t8: Tuple8[_, _, _, _, _, _, _, _] => Option(t8)
    case t9: Tuple9[_, _, _, _, _, _, _, _, _] => Option(t9)
    case t10: Tuple10[_, _, _, _, _, _, _, _, _, _] => Option(t10)
    case t11: Tuple11[_, _, _, _, _, _, _, _, _, _, _] => Option(t11)
    case t12: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _] => Option(t12)
    case t13: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t13)
    case t14: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t14)
    case t15: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t15)
    case t16: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t16)
    case t17: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t17)
    case t18: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t18)
    case t19: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t19)
    case t20: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t20)
    case t21: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t21)
    case t22: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => Option(t22)
    case _ => None
  }
}


object BinWrapper {

  implicit def materializeBinWrapper[T]: BinWrapper[T] = macro materializeBinWrapperImpl[T]

  def materializeBinWrapperImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[BinWrapper[T]] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val one = weakTypeOf[(String, T)]
    val singleton = weakTypeOf[SingleBin[T]]
    val multi = weakTypeOf[MBin[T]]
    val many = weakTypeOf[Map[String, T]]
    val out = weakTypeOf[(Map[String, Option[T]], Int, Int)]
    val tpeSt = q"${tpe.toString}"

    def mp(add: Tree) =
      q"""override def fetch(any: Any): Option[$tpe] = any match {
                        case v: $tpe => Option(v)
                        case any: Any => scala.util.Try{$add}.toOption
                        case oth => None
                       }
       """

    def tupleFetch(ts: List[String]) =
      q"""
                      override def fetch(any: Any): Option[$tpe] = {
                                      any match {
                                        case m: java.util.HashMap[Any, Any] =>
                                               val res = castTuple(m.asScala.toMap, $ts)
                                               res.collect{ case t: $tpe => t }
                                        case _ => None
                                      }
           }"""

    val mh =
      q"""override def fetch(any: Any): Option[$tpe] = any match {
             case m: java.util.HashMap[Any, Any] =>
             val newList = castHListElements(m.asScala.values.toList, $tpeSt)
             newList.toHList[$tpe]
             case oth => None
           }
        """

    def tupleArity(tpe: Type): Int = {
      val rex = "Tuple(\\d{1,2})".r
      tpe.typeSymbol.name.encodedName.toString match {
        case rex(n) if n.toInt > 1 && n.toInt < 23 => n.toInt
        case _ => 0
      }
    }

    val fetchValue = tpe match {
      case t if t.toString.contains("HNil") || t.toString.contains("HList") => mh
      case t if t =:= weakTypeOf[String] => q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: String => Option(v)
        case oth => scala.util.Try(oth.toString).toOption
     } """
      case t if t =:= weakTypeOf[Boolean] => q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.lang.Long => Option(v == 1)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Float] => q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.lang.Double => Option(v.toFloat)
        case v: java.lang.Float => Option(v)
        case oth => scala.util.Try(oth.asInstanceOf[Float]).toOption
     } """
      case t if t =:= weakTypeOf[Char] => q"""override def fetch(any: Any): Option[$tpe] = any match {
      case v: String => v.toString.toCharArray.headOption
      case oth => scala.util.Try(oth.toString.toCharArray.head).toOption
     } """
      case t if t =:= weakTypeOf[Int] => q"""override def fetch(any: Any): Option[$tpe] = any match {
      case v: java.lang.Long => Option(v.toInt)
      case oth => scala.util.Try(oth.toString.toInt).toOption
     } """
      case t if t =:= weakTypeOf[Short] => q"""override def fetch(any: Any): Option[$tpe] = any match {
       case v: java.lang.Long => Option(v.toShort)
       case oth => scala.util.Try(oth.asInstanceOf[Long].toShort).toOption
     } """
      case t if t =:= weakTypeOf[Byte] => q"""override def fetch(any: Any): Option[$tpe] = {
         any match {
               case v: java.lang.Long => Option(v.toByte)
               case _ => None
          }
        }"""
      case t if t =:= weakTypeOf[List[String]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[String] => Option(v.toList)
        case _ => None
     } """
      case t if t =:= weakTypeOf[List[Int]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Int] => Option(v.toList)
        case _ => None
     } """
      case t if t =:= weakTypeOf[List[Long]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Long] => Option(v.toList)
        case _ => None
     } """
      case t if t =:= weakTypeOf[List[Float]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Double] => Option(v.view.map(_.toFloat).toList)
        case _ => None
     } """
      case t if t =:= weakTypeOf[List[Double]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Double] => Option(v.view.map(_.toDouble).toList)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Array[String]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[String] => Option(v.asScala.toArray)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Array[Int]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Long] => Option(v.asScala.view.map(_.toInt).toArray)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Array[Long]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Long] => Option(v.asScala.toArray)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Array[Float]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Double] => Option(v.asScala.toArray[Double].view.map(_.toFloat).toArray)
        case _ => None
     } """
      case t if t =:= weakTypeOf[Array[Double]] =>
        q"""override def fetch(any: Any): Option[$tpe] = any match {
        case v: java.util.ArrayList[Double] => Option(v.asScala.toArray[Double])
        case _ => None
     } """
      case t if t =:= weakTypeOf[Map[Int, String]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
               case v: java.util.HashMap[Any, String] => scala.util.Try(v.view.map{
                 case (k, v) => k.toString.toInt -> v
               }.toMap).toOption
               case _ => None
             }
         """
      case t if t =:= weakTypeOf[Map[String, String]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
                   case v: java.util.HashMap[String, String] => Option(v.toMap)
                   case _ => None
                }
         """
      case t if t =:= weakTypeOf[Map[String, Int]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.util.HashMap[String, Any] => scala.util.Try(v.view.map{
                case (k, v) => k -> v.toString.toInt
              }.toMap).toOption
              case _ => None
            }
         """
      case t if t =:= weakTypeOf[Map[String, Long]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.util.HashMap[String, Long] => scala.util.Try(v.toMap).toOption
              case _ => None
            }
         """
      case t if t =:= weakTypeOf[Map[String, Float]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.util.HashMap[String, Any] => scala.util.Try(v.view.map{
                case (k, v) => k -> v.toString.toFloat
              }.toMap).toOption
              case _ => None
            }
         """
      case t if t =:= weakTypeOf[Map[String, Double]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.util.HashMap[String, Any] => scala.util.Try(v.view.map{
                case (k, v) => k -> v.toString.toDouble
              }.toMap).toOption
              case _ => None
            }
         """
      case t if t =:= weakTypeOf[Map[String, List[Int]]] => mp(q"""toKVmap[String, List[Int]](any, coll)(_.toString, toLs(_)(_.toInt))""")
      case t if t =:= weakTypeOf[Map[String, List[String]]] => mp(q"""toKVmap[String, List[String]](any, coll)(_.toString, toLs(_))""")
      case t if t =:= weakTypeOf[Map[String, Any]] =>
        q""" override def fetch(any: Any): Option[$tpe] = any match {
              case v: java.util.HashMap[String, Any] => scala.util.Try(v.asScala.toMap).toOption
              case _ => None
            }
         """
      case t if tupleArity(t) != 0 =>
        val tplArty = tupleArity(t)
        if (tplArty > 0 && tplArty < 23) tupleFetch(t.typeArgs.map(_.toString)) else q"""None"""
      case _ => q""""""
    }

    c.Expr[BinWrapper[T]] {
      q"""

      import java.util.{List => JList, Map => JMap}
      import com.aerospike.client.{Bin, Record, Value}
      import com.aerospike.client.Value.{BlobValue, ListValue, MapValue, ValueArray}
      import scala.collection.JavaConversions._
      import scala.collection.JavaConverters._
      import scala.collection.mutable.{Seq => mSeq}
      import scala.language.experimental.macros
      import shapeless.{HList, _}
      import shapeless.HList.hlistOps
      import syntax.std.traversable._
      import scala.collection.immutable.ListMap
      import ru.tinkoff.aerospikemacro.cast.Caster._
      import ru.tinkoff.aerospikemacro.converters._

      new BinWrapper[$tpe] {
        override def apply(one: $one): Bin = if (one._1.length > 14) throwE("Current limit for bean name is 14 characters") else gen(one)
        override def apply(many: $multi): List[Bin] = many.asOne.view.map(apply).toList
        override def apply(one: $singleton): Bin = apply((one.name, one.value))
        override def apply(many: $many): List[Bin] = many.view.flatMap(one => scala.util.Try(apply(one)).toOption).toList
        override def apply(r: Record): $out = {
           val outValue: Map[String, Option[$tpe]] = {
           val jMap = r.bins.view collect {
            case (name, bt: Any) =>
            val res = fetch(bt)
            if (res.isEmpty && r.bins.nonEmpty) throwClassCast($tpeSt) else name -> res
           }
          jMap.toMap
          }

         (outValue, r.generation, r.expiration)
        }
        $fetchValue
      }

    """
    }
  }
}
