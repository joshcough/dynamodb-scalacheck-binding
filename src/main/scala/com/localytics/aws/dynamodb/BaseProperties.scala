package com.localytics.aws.dynamodb

import org.scalacheck.{Gen, Prop, Properties}
import org.scalacheck.Prop._

abstract class BaseProperties(name: String) extends Properties(name) {

  def test(name: String)(f: => Prop): Unit = {
    property(name) = secure(f)
    ()
  }

  def resize[T](maxSize: Int = 75, reduceFactor: Int = 2)(g:Gen[T]): Gen[T] =
    Gen.sized { size =>
      if(size < maxSize) g
      else Gen.resize(size / reduceFactor, g)
    }

  // strGen generates a fixed length random string
  def strGen(n: Int): Gen[String] =
    Gen.listOfN(n, Gen.alphaChar).map(_.mkString)
}
