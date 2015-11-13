package com.localytics.aws.dynamodb.updates

import com.localytics.aws.dynamodb.BaseProperties
import org.scalacheck.Prop._
import scalaz.std.anyVal._
import scalaz.std.string._
import DiffArbitrary._

object UpdateProperties extends BaseProperties("Update") {
  test("update round trip")(forAll{ d: Diff[Int, String] =>
    val Diff(oldV,newV,diffs) = Update.applyUpdates(d.oldValues, d.diffs)
    val ups: Set[Update[Int, String]] = Update.getUpdates(oldV, newV)
    diffs == ups
  })
}
