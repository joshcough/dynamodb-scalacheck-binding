package com.localytics.aws.dynamodb.updates

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck._

import scala.language.implicitConversions
import scalaz.Equal
import scalaz.scalacheck.ScalaCheckBinding._
import scalaz.std.list.listInstance
import scalaz.syntax.traverse._

object UpdateArbitrary {

  /**
   * Create an arbitrary Add, Delete, Modify, Unmodified
   */
  implicit def arbUpdate[K,A](key: K, oldValue: A)
                             (implicit g: Arbitrary[A],
                                       eq: Equal[A]): Arbitrary[Update[K,A]] =
    Arbitrary(arbitrary[A].flatMap(a => oneOf(
      Add(key, a),
      Delete(key, oldValue),
      // this double checks that we don't produce a Modify with
      // oldValue == a
      Update.modify(key, oldValue, a),
      Unmodified(key, oldValue)
    )))
}

object DiffArbitrary {

  /**
   * Create an arbitrary Diff by:
   *   - creating a Map[K, V]
   *   - creating arbitrary updates for each key
   *   - applying those updates map to create a new map
   */
  implicit def arbDiff[K,V](implicit gk: Arbitrary[K], gv: Arbitrary[V],
                                     eq: Equal[V]): Arbitrary[Diff[K,V]] =
    Arbitrary(for {
      map   <- arbitrary[Map[K,V]]
      edits <- map.toList.traverse { case (k,v) =>
        UpdateArbitrary.arbUpdate(k,v).arbitrary
      }.map(_.toSet)
    } yield Update.applyUpdates(map, edits))
}