package com.localytics.aws.dynamodb

import org.scalacheck.{Gen, Prop, Properties}
import org.scalacheck.Prop._

abstract class BaseProperties(name: String) extends Properties(name) {

  def test(name: String)(f: => Prop): Unit = {
    property(name) = secure(f)
    ()
  }

  // TODO: this was in use in profiles, but since i moved this out of there
  // i now need tests for arbitrary diffs. then this function would be used.
  def resize[T](maxSize: Int = 75, reduceFactor: Int = 2)(g:Gen[T]): Gen[T] =
    Gen.sized { size =>
      if(size < maxSize) g
      else Gen.resize(size / reduceFactor, g)
    }

  // strGen generates a fixed length random string
  def strGen(n: Int): Gen[String] =
    Gen.listOfN(n, Gen.alphaChar).map(_.mkString)
}
