package com.localytics.aws.dynamodb.updates

import scala.language.implicitConversions
import scalaz.{Equal, Order}
import scalaz.syntax.equal._

sealed trait Update[K,A] { val key: K }
  case class Add[K,A](key: K, value: A) extends Update[K,A]
  case class Delete[K,A](key: K, oldValue: A) extends Update[K,A]
  case class Modify[K,A](key: K, oldValue: A, newValue: A) extends Update[K,A]
  case class Unmodified[K,A](key: K, value: A) extends Update[K,A]

object Diff {
  def apply[K,V](t: (Map[K,V], Map[K,V]), diffs: Set[Update[K,V]]): Diff[K,V] =
    new Diff(t._1, t._2, diffs)
}

/**
  *
  * @param oldValues The original record
  * @param newValues The new record with potentially updated
  *                  values from oldValues
  * @param diffs
  * @tparam K
  * @tparam V
  */
case class Diff[K,V](oldValues: Map[K,V],
                     newValues: Map[K,V],
                     diffs: Set[Update[K,V]])

object Update {

  /**
   * Creates a Modify if oldValue /== newValue
   * Otherwise, creates an Unmodified.
   */
  def modify[K,V](key: K, oldValue: V, newValue: V)
                (implicit eq: Equal[V]): Update[K,V] = {
    if(newValue /== oldValue) Modify(key, oldValue, newValue)
    else Unmodified(key, oldValue)
  }

  def isMod[K,V](u:Update[K,V]): Boolean = u match {
    case Unmodified(_,_) => false
    case _               => true
  }

  /**
   * Generate a new attribute value for all the given updates.
   * And then use those updates and values to
   * apply them to the given old image.
   */
  def applyUpdates[K,V](oldValues: Map[K,V],
                        updates:Set[Update[K, V]]): Diff[K,V] =
    Diff(updates.foldLeft(oldValues, oldValues)(applyUpdateT),updates)

  def applyUpdateT[K,V](t: (Map[K,V], Map[K,V]),
                        update: Update[K,V]): (Map[K,V], Map[K,V]) =
    applyUpdate(t._1, t._2, update)

  /**
   * Apply the given updates to the given maps.
   */
  def applyUpdate[K,V](oldValues: Map[K,V],
                       newValues: Map[K,V],
                       update: Update[K,V]): (Map[K,V], Map[K,V]) =
    update match {
      // remove the key from the old map, leave it in the new one
      // which gives the appearance that it was added.
      case Add(k,v)        => (oldValues - k, newValues + (k -> v))
      // remove the key from the new map, leave it in the old one
      // which gives the appearance that it was removed
      case Delete(k,_)     => (oldValues, newValues - k)
      // nothing changes
      case Unmodified(_,_) => (oldValues, newValues)
      // leave the key in both maps, change the value in the new map.
      case Modify(k,o,n)   => (oldValues, newValues + (k -> n))
    }

  /**
   * Compare the two maps, and return the diffs between them.
   */
  def getUpdates[K,V](oldValues: Map[K,V], newValues: Map[K,V])
                     (implicit eqK: Order[K],
                               eqV: Order[V]): Set[Update[K,V]] = {
    val deletedKeys = oldValues.keySet -- newValues.keySet
    val addedKeys   = newValues.keySet -- oldValues.keySet
    val commonKeys  = oldValues.keySet.intersect(newValues.keySet)
    val (unmodifiedKeys,modifiedKeys) = commonKeys.partition(k =>
      eqV.equal(oldValues(k), newValues(k))
    )

    val deleted    = deletedKeys   .map(k => Delete(k, oldValues(k)))
    val added      = addedKeys     .map(k => Add(k, newValues(k)))
    val unmodified = unmodifiedKeys.map(k => Unmodified(k, oldValues(k)))
    val modified   = modifiedKeys  .map(k => Modify(k, oldValues(k), newValues(k)))

    deleted ++ added ++ unmodified ++ modified
  }

  implicit def eqUpdate[K,A](implicit eqK: Equal[K],
                                      eqA: Equal[A]): Equal[Update[K,A]] =
    Equal.equalA[Update[K,A]]
}
