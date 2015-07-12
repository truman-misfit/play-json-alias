package com.truman.modules.json.alias

import play.api._
import play.api.test._
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

class JsonAliasSpec extends PlaySpecification {

  "A JSON Alias specification" should {
    "Normal encode and decode" in new WithApplication {

      val jalias = app.injector.instanceOf[JsonAlias]

      case class DemoJSON(
        oneLongAttribute: String,
        anotherLongAttribute: String,
        oneAnotherLongAttribute: String
      )
      implicit val DemoJSONFormat = Json.format[DemoJSON]

      val demoJSONObject = DemoJSON(
        "attribute", "another attribute",
        "another once again attribute"
      )
      val jsonOfDemoObject = Json.toJson(demoJSONObject)
                                .as[JsObject]

      val resultEncodedJSON = Await.result(jalias.encode(jsonOfDemoObject), 10.millis)
      Logger.info("The encoded JSON: " + resultEncodedJSON)
      val resultDecodedJSON = Await.result(jalias.decode(resultEncodedJSON), 10.millis)
      Logger.info("The decoded JSON: " + resultDecodedJSON)

      // parse to scala Object
      resultDecodedJSON.validate[DemoJSON] match {
        case s: JsSuccess[DemoJSON] => {
          s.get must_== demoJSONObject
        }
        case e: JsError => {
          throw new Exception("Errors: " + JsError.toFlatJson(e).toString())
        }
      }
    }
  }
}
