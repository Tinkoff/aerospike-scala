package ru.tinkoff.aerospikemacro.converters

import ru.tinkoff.aerospikescala.domain.ByteSegment

import scala.reflect.macros.blackbox.Context

/**
  * @author MarinaSigaeva 
  * @since 04.04.17
  */
object Utils {
  def pickValue[T: c.WeakTypeTag](c: Context): c.universe.Tree = {
    import c.universe._
    val tpe = weakTypeOf[T]
    val tpeName = q"${tpe.typeSymbol.fullName}"

    val err =
      q"""throw new IllegalArgumentException(
         "You need to write your own toValue function in KeyWrapper implicit for type " + $tpeName) """

    tpe match {
      case t if t =:= weakTypeOf[ByteSegment] => q"""v match {
        case ByteSegment(bytes, offset, length) => new ByteSegmentValue(bytes, offset, length)
        case _ => $err
        }"""
      case t if t =:= weakTypeOf[Int] => q"""new IntegerValue(v)"""
      case t if t =:= weakTypeOf[Long] => q"""new LongValue(v)"""
      case t if t =:= weakTypeOf[String] => q"""new StringValue(v)"""
      case t if t =:= weakTypeOf[Boolean] => q"""new BooleanValue(v)"""
      case t if t =:= weakTypeOf[Float] => q"""new FloatValue(v)"""
      case t if t =:= weakTypeOf[Double] => q"""new DoubleValue(v)"""
      case t if t =:= weakTypeOf[Array[Byte]] => q"""new BytesValue(v)"""
      case t if t =:= weakTypeOf[scala.collection.immutable.Seq[_]] => q"""new ListValue(v)"""
      case t if t =:= weakTypeOf[Map[_, _]] => q"""new MapValue(v)"""
      case _ => q"""Try(Value.get(v)) match {
      case Failure(th) => $err
      case Success(s) => s
    }"""
    }
  }
}