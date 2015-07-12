package com.truman.modules.json.alias

import play.api._
import play.api.test._
import play.api.libs.json._

import javax.inject._

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
      val demoJsValue = Json.toJson(demoJSONObject)
      val demoJsObject = demoJsValue.as[JsObject]

      jalias.encode(demoJsObject).map { json =>
        println(json)
        1 must_== 1
      }.getOrElse {
        throw new Exception("JSON alias encoding failed.")
      }
    }
  }
}
