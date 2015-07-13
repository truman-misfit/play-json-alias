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
  val complexMockSeq: Seq[Seq[MockEmbedObject]] = Seq(embedMockSeq)

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
  val complexMockJsArray = Json.toJson(complexMockSeq)
                              .as[JsArray]

  "A JSON Alias specification" should {
    "JsObject encode and decode" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(jalias.encode(oneMockJsObject), 10.millis)
      val resultDecodedJSON = Await.result(jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The simple JsObject alias decode/encode: ")
      Logger.info("------ JSON ------")
      Logger.info(Json.prettyPrint(resultEncodedJSON))
      Logger.info("------ Alias ------")
      Logger.info(Json.prettyPrint(resultDecodedJSON))
      Logger.info("-------------------")

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
      val resultDecodedJSON = Await.result(jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The simple JsArray alias decode/encode: ")
      Logger.info("------ JSON ------")
      Logger.info(Json.prettyPrint(resultEncodedJSON))
      Logger.info("------ Alias ------")
      Logger.info(Json.prettyPrint(resultDecodedJSON))
      Logger.info("-------------------")

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

    "JsObject embedded another JsObject" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(
            jalias.encode(embedMockJsObject), 10.millis)
      val resultDecodedJSON = Await.result(
            jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The embedded JsObject alias decode/encode: ")
      Logger.info("------ JSON ------")
      Logger.info(Json.prettyPrint(resultEncodedJSON))
      Logger.info("------ Alias ------")
      Logger.info(Json.prettyPrint(resultDecodedJSON))
      Logger.info("-------------------")

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

    "JsArray embed another JsObject" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(
            jalias.encode(embedMockJsArray), 10.millis)
      val resultDecodedJSON = Await.result(
            jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("JsArray contains JsObject alias decode/encode: ")
      Logger.info("------ JSON ------")
      Logger.info(Json.prettyPrint(resultEncodedJSON))
      Logger.info("------ Alias ------")
      Logger.info(Json.prettyPrint(resultDecodedJSON))
      Logger.info("-------------------")

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

    "JsArray embed another JsArray" in new WithApplication {
      val jalias = app.injector.instanceOf[JsonAlias]

      val resultEncodedJSON = Await.result(
            jalias.encode(complexMockJsArray), 10.millis)
      val resultDecodedJSON = Await.result(
            jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("JsArray contains another JsArray alias decode/encode: ")
      Logger.info("------ JSON ------")
      Logger.info(Json.prettyPrint(resultEncodedJSON))
      Logger.info("------ Alias ------")
      Logger.info(Json.prettyPrint(resultDecodedJSON))
      Logger.info("-------------------")

      resultDecodedJSON.validate[Seq[Seq[MockEmbedObject]]] match {
        case s: JsSuccess[Seq[Seq[MockEmbedObject]]] => {
          val headOfHeadOfEmbedSeq = s.get.head.head
          headOfHeadOfEmbedSeq.oneObject must_== oneMockObject
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString)
        }
      }
    }
  }
}
