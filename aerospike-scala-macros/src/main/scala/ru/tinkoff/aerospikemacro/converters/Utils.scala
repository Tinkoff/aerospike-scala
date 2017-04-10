package ru.tinkoff.aerospikemacro.converters

import com.aerospike.client.Value
import com.aerospike.client.Value._
import ru.tinkoff.aerospikescala.domain.ByteSegment
import shapeless.{::, HList}

import scala.collection.immutable.{List, ListMap, Map}
import scala.reflect.ClassTag
import scala.reflect.macros.blackbox
import scala.util.{Failure, Success}
import scala.reflect.runtime.universe._
import scala.collection.JavaConverters._


/**
  * @author MarinaSigaeva 
  * @since 04.04.17
  */
object Utils {

  def isTuple[T](x: T) = x.getClass.getSimpleName.contains("Tuple")

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

  def typed[T](x: T)(implicit tag: WeakTypeTag[T]): T = x.asInstanceOf[T]

  def fromHList[L <: HList](hList: L, i: Int, maxIndex: Int): Map[String, Any] = {
    val h0 = Map(i.toString -> typed(hList.productElement(0)))
    hList match {
      case head :: tail if i < maxIndex => h0 ++ fromHList(tail, i + 1, maxIndex)
      case _ => h0
    }
  }

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


  def throwIllegal(w: String, t: String): Tree = {
    val tpeName = q"$t"

      q"""throw new IllegalArgumentException(
         "You need to write your own toValue function in " + $w + " implicit for type " + $tpeName) """
  }

  def pickValue[T: c.WeakTypeTag](c: blackbox.Context, wrapper: String): c.universe.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tpeName = q"${tpe.typeSymbol.fullName}"

    val err =
      q"""throw new IllegalArgumentException(
         "You need to write your own toValue function in " + $wrapper + " implicit for type " + $tpeName)"""

    tpe match {
      case t if t =:= weakTypeOf[ByteSegment] => q"""v match {
        case ByteSegment(bytes, offset, length) => new ByteSegmentValue(bytes, offset, length)
        case _ => $err
        }"""
      case t if t =:= weakTypeOf[Int] => q"""new IntegerValue(v)"""
      case t if t =:= weakTypeOf[Short] => q"""new IntegerValue(v)"""
      case t if t =:= weakTypeOf[Byte] => q"""new IntegerValue(v)"""
      case t if t =:= weakTypeOf[Long] => q"""new LongValue(v)"""
      case t if t =:= weakTypeOf[String] => q"""new StringValue(v)"""
      case t if t =:= weakTypeOf[Char] => q"""new StringValue(v.toString)"""
      case t if t =:= weakTypeOf[Boolean] => q"""new BooleanValue(v)"""
      case t if t =:= weakTypeOf[Float] => q"""new FloatValue(v)"""
      case t if t =:= weakTypeOf[Double] => q"""new DoubleValue(v)"""
      case t if t =:= weakTypeOf[java.util.List[_]] => q"""new ListValue(v)"""
      case t if t =:= weakTypeOf[List[_]] => q"""new ListValue(v)"""
      case t if t =:= weakTypeOf[Array[_]] => q"""new ListValue(v.toList)"""
      case t if t =:= weakTypeOf[java.util.Map[_, _]] => q"""new MapValue(v)"""
      case t if t =:= weakTypeOf[Array[Byte]] => q"""new BytesValue(v)"""
      case t if t =:= weakTypeOf[scala.collection.immutable.Seq[_]] => q"""new ListValue(v)"""
      case t if t =:= weakTypeOf[Map[_, _]] => q"""new MapValue(v)"""
      case t: Any with Product if isTuple(t) => q"""new MapValue(tupleMapped($tpe))"""
      case yourCaseClass => q""" Value.get(v) match {
                                   case n: NullValue => $err
                                   case other => other
                                 }"""

    }
  }
}