package com.localytics.aws.dynamodb

import com.amazonaws.services.dynamodbv2.model.{AttributeValue, StreamRecord}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck._

import scala.collection.JavaConverters._
import scalaz.{Equal, Show}

/**
 * Generators and Arbitrary instances for Dynamo values:
 * AttributeValue, Map[FieldName, AttributeValue, DynamodbStreamRecord
 */
object DynamoArbitrary {

  import DiffArbitrary._
  import DynamoConverters._

  type Record[K] = Map[K, AttributeValue]
  type DynamoRecord = Record[String]
  type JDynamoRecord = java.util.Map[String, AttributeValue]

  implicit val eqAttributeValue: Equal[AttributeValue] =
    Equal.equalA[AttributeValue]

  /**
   * Generate a Dynamo AttributeValue
   */
  implicit val arbAttrVal: Arbitrary[AttributeValue] = {
    val genString: Gen[String] =
      // TODO: I changed the constant here to 7 to make the
      // test output more readable. might want to change it back
      choose(0, 7).flatMap(n => listOfN(n, alphaNumChar)).map(_.mkString)
    Arbitrary(oneOf(
      arbitrary[Long].map(_.asDynamo),
      genString.map(_.asDynamo),
      nonEmptyListOf(arbitrary[Long]).map(_.toSet[Long].asDynamo),
      nonEmptyListOf(genString).map(_.toSet[String].asDynamo)
    ))
  }

  /**
   * Generate a Dynamo record (Map[K, AttributeValue])
   */
  implicit def arbDynamoRecord[K]: Arbitrary[Record[K]] =
    Arbitrary(arbitrary[Map[K, AttributeValue]])

  /**
   * Generate a DynamodbStreamRecord,
   * paired with the diffs made to create it the new image.
   */
  implicit def arbStreamRecord[K](implicit arb: Arbitrary[K], s: Show[K]):
    Arbitrary[(DynamodbStreamRecord, Set[Update[K, AttributeValue]])] =
      Arbitrary(arbDynamoDiff[K].arbitrary.map(d => (d.rec, d.diff.diffs)))

  /**
   *
   */
  implicit def arbDynamoDiff[K](implicit arb: Arbitrary[K], s: Show[K]):
    Arbitrary[DynamoDiff[K]] = Arbitrary(for {
      seqNum <- arbitrary[Int]
      diff   <- arbDiff[K, AttributeValue].arbitrary
    } yield DynamoDiff(diff, seqNum))

  /**
   * Make a DynamodbStreamRecord containing the old and new images,
   * and the given sequence number.
   * This would be the place to add other things to the record,
   * if needed.
   */
  def makeStreamRecord[K]
    (oldImage: Record[K], newImage: Record[K], seqNum: Int)
    (implicit show: Show[K]): DynamodbStreamRecord = {
    def toJava(m: Map[K, AttributeValue]): JDynamoRecord =
      m.map{ case (f,v) => (f.toString, v) }.asJava
    new DynamodbStreamRecord() {
      override def getDynamodb: StreamRecord = new StreamRecord {}
        .withOldImage(toJava(oldImage))
        .withNewImage(toJava(newImage))
        .withSequenceNumber(seqNum.toString)
    }
  }

  object DynamoDiff {
    def apply[K](diff: Diff[K, AttributeValue],
                 seqNum: Int)(implicit show: Show[K]): DynamoDiff[K] =
      DynamoDiff(
        makeStreamRecord(diff.oldValues, diff.newValues, seqNum),
        diff
      )
  }

  case class DynamoDiff[K](rec: DynamodbStreamRecord,
                           diff: Diff[K, AttributeValue]) {

    override def toString: String = diff.toString

    def dynamo: StreamRecord = rec.getDynamodb
    def seq: Int = dynamo.getSequenceNumber.toInt
    def oldValues: DynamoRecord = dynamo.getOldImage.asScala.toMap
    def newValues: DynamoRecord = dynamo.getNewImage.asScala.toMap

    /**
     * Add the given key->val pair to
     *   the old and new images in the dynamo record
     *   and for consistency
     *     the old and new images in the diff
     *     the set of updates (as Unmodified)
     */
    def add(kv: (K, AttributeValue))(implicit show: Show[K]): DynamoDiff[K] = {
      DynamoDiff(
        makeStreamRecord[K](diff.oldValues + kv, diff.newValues + kv, seq),
        Diff(
          diff.oldValues + kv, diff.newValues + kv,
          diff.diffs + Unmodified(kv._1, kv._2)
        )
      )
    }
  }
}
