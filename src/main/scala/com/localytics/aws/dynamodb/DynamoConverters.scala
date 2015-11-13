package com.localytics.aws.dynamodb

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.collection.JavaConverters._

object DynamoConverters {
  implicit class AttrString(s:String) {
    def asDynamo: AttributeValue = new AttributeValue().withS(s)
  }
  implicit class AttrNumber(n: Number) {
    def asDynamo: AttributeValue = new AttributeValue().withN(n.toString)
  }
  implicit class AttrLong(long:Long) {
    def asDynamo: AttributeValue = new AttributeValue().withN(long.toString)
  }
  implicit class AttrStringSet(arr: Set[String]) {
    def asDynamo: AttributeValue = new AttributeValue().withSS(arr.asJava)
  }
  implicit class AttrLongSet(arr: Set[Long]) {
    def asDynamo: AttributeValue =
      new AttributeValue().withNS(arr.map(_.toString).asJava)
  }
}