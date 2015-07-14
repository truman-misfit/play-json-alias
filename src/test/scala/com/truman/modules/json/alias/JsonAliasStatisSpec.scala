package com.truman.modules.json.alias

import play.api._
import play.api.test._
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.mutable.ListBuffer

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

class JsonAliasStatisSpec extends PlaySpecification {
  // mock
  val fakeContent = JsString("content")
  val mockTestObjectListBuffer = ListBuffer[JsObject]()
  val maxAttrAvgLength = 50

  "A statis report about alias strategy" should {
    "go through all increment avg attribute legnth test case" in new WithApplication {
      // fake all test cases
      for (i <- 1 to maxAttrAvgLength) {
        var attr = "a" * i
        val obj = JsObject(Seq(
          ("a" * i, fakeContent),
          ("b" * i, fakeContent),
          ("c" * i, fakeContent)
        ))
        mockTestObjectListBuffer += obj
      }

      val jalias = app.injector.instanceOf[JsonAlias]
      for (i <- 1 to maxAttrAvgLength) {
          val jsonTestObject = mockTestObjectListBuffer(i - 1)
          val aliasTestObject = Await.result(jalias.encode(jsonTestObject), 10.millis)
          val comprRate = ((jsonTestObject.toString.length -
                            aliasTestObject.toString.length).
                              toFloat/jsonTestObject.toString.length) * 100
          printf("Average Attribute Length: " +
                  i +
                  " - compressed rate: %2.4f%%\n", comprRate)
      }
      1 must_== 1
    }
  }
}
