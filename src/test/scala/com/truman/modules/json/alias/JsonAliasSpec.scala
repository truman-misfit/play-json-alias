package com.truman.modules.json.alias

import play.api._
import play.api.test._
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

class JsonAliasSpec extends PlaySpecification {

  case class MockObject(
    oneLongAttribute: String,
    anotherLongAttribute: String,
    oneAnotherLongAttribute: String
  )
  case class MockEmbedObject(
    oneLongAttribute: String,
    anotherLongAttribute: String,
    oneObject: MockObject,
    anotherObject: MockObject
  )

  implicit val MockObjectFormat = Json.format[MockObject]
  implicit val MockEmbedObjectFormat = Json.format[MockEmbedObject]

  // cover JsObject test case
  val oneMockObject = MockObject(
    "attribute", "another attribute",
    "another once again attribute"
  )

  val anotherMockObject = MockObject(
    "long attribute", "long long attribute",
    "another long long attribute"
  )
  // cover JsArray test case
  val mockObjectSeq = Seq(oneMockObject, anotherMockObject)
  // cover embedded JSON structure of JsObject test case
  val embedMockObject = MockEmbedObject(
    "attribute", "another attribute",
    oneMockObject, anotherMockObject
  )
  // cover embedded JSON structure of JsArray test case
  val embedMockSeq: Seq[MockEmbedObject] = Seq(embedMockObject)

  val oneMockJsObject = Json.toJson(oneMockObject)
                            .as[JsObject]
  val anotherMockJsObject = Json.toJson(anotherMockObject)
                                .as[JsObject]
  val mockJsArray = Json.toJson(mockObjectSeq)
                        .as[JsArray]
  val embedMockJsObject = Json.toJson(embedMockObject)
                              .as[JsObject]
  val embedMockJsArray = Json.toJson(embedMockSeq)
                            .as[JsArray]

  "A JSON Alias specification" should {
    "JsObject encode and decode" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(jalias.encode(oneMockJsObject), 10.millis)
      Logger.info("The encoded JsObject: " + resultEncodedJSON)
      val resultDecodedJSON = Await.result(jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The decoded JsObject: " + resultDecodedJSON)

      // parse to scala Object
      resultDecodedJSON.validate[MockObject] match {
        case s: JsSuccess[MockObject] => {
          s.get must_== oneMockObject
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString)
        }
      }
    }

    "JsArray encode and decode" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(jalias.encode(mockJsArray), 10.millis)
      Logger.info("The encoded JsArray: " + Json.prettyPrint(resultEncodedJSON))
      val resultDecodedJSON = Await.result(jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The decoded JsArray: " + Json.prettyPrint(resultDecodedJSON))

      // parse to scala Object
      resultDecodedJSON.validate[Seq[MockObject]] match {
        case s: JsSuccess[Seq[MockObject]] => {
          val decodedMockObjectSeq = s.get
          decodedMockObjectSeq.size must_== mockObjectSeq.size
          decodedMockObjectSeq must_== mockObjectSeq
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString)
        }
      }
    }

    "JsObject embedded JSON structure encode and decode" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(
            jalias.encode(embedMockJsObject), 10.millis)
      Logger.info("The encoded embedded JsObject: " + Json.prettyPrint(resultEncodedJSON))
      val resultDecodedJSON = Await.result(
            jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The decoded embedded JsObject: " + Json.prettyPrint(resultDecodedJSON))

      resultDecodedJSON.validate[MockEmbedObject] match {
        case s: JsSuccess[MockEmbedObject] => {
          val mockEmbedObject = s.get
          mockEmbedObject.oneObject must_== oneMockObject
          mockEmbedObject.anotherObject must_== anotherMockObject
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString)
        }
      }
    }

    "JsArray embedded JSON structure encode and decode" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(
            jalias.encode(embedMockJsArray), 10.millis)
      Logger.info("The encoded embedded JsArray: " + Json.prettyPrint(resultEncodedJSON))
      val resultDecodedJSON = Await.result(
            jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The encoded embedded JsArray: " + Json.prettyPrint(resultDecodedJSON))

      resultDecodedJSON.validate[Seq[MockEmbedObject]] match {
        case s: JsSuccess[Seq[MockEmbedObject]] => {
          val headMockEmbedObjectInSeq = s.get.head
          headMockEmbedObjectInSeq.oneObject must_== oneMockObject
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString)
        }
      }
    }
  }
}
