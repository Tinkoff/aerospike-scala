package ru.tinkoff.aerospike.proto

import scala.reflect.macros.blackbox
import scala.language.experimental.macros

/**
  * @author MarinaSigaeva 
  * @since 27.03.17
  */
trait ArrayParser[T] {
   //val packageName: String = ""
   def parse: (Array[Byte] => T)
}


object ArrayParser {

   implicit def materialize[T]: ArrayParser[T] = macro materializeImpl[T]

   def materializeImpl[T: c.WeakTypeTag](c: blackbox.Context): c.Expr[ArrayParser[T]] = {
      import c.universe._
      val tpe = weakTypeOf[T]
      println("ArrapParser is started " + tpe)
      val tpeSt = q"${tpe.toString}"

      val pack = q"${tpe.typeConstructor}" // + "parseFrom(arr)"
      //val parseArray = ValDef(tpe.typeSymbol.fullName.dropRight(tpeLen) + "parseFrom(arr)")
      val imports = q"""import $pack._"""
      val parseFunction = q"""$pack.parseFrom"""

      val imports2 = q"""  """

      val re = c.Expr[ArrayParser[T]] {
         q"""
          new ArrayParser[$tpe] {
            override def parse: (Array[Byte] => $tpe) = parseFunction
          }
    """
      }

      println("ArrayParser is here " + re)
      re
   }
}
