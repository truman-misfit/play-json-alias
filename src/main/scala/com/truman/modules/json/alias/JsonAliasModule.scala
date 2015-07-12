package com.truman.modules.json.alias

import java.util.Date
import javax.inject.{Inject, Singleton}
import com.google.inject.AbstractModule
import scala.concurrent.Future

import play.api.libs.json._
import play.api.{ Logger, Environment, Configuration }

case class Alias(
  id: String, // This is the "hash"
  src: String, // The original string
  time: Date = new Date // Init timestamp
)
object Alias {
  implicit val aliasJSONFormat = Json.format[Alias]
}

trait JsonAlias {
  /**
   * Alias for JsObject attributes.
   *
   * @param raw alias for JsObject
   * @return The aliased JsObject.
   */
  def encode(jsObject: JsObject): Future[JsObject]

  /**
   * Alias for JsObject's attributes in JsArray
   *
   * @param array alias for JsArray
   * @return The aliased JsArray.
   */
  def encode(jsArray: JsArray): Future[JsArray]

  /**
   * decode aliased JsObject to plain one.
   *
   * @param alias The aliased JsObject.
   * @return The original JsObject.
   */
  def decode(aliasObject: JsObject): Future[JsObject]

  /**
   * decode aliased JsArray to plain JsArray.
   *
   * @param alias The aliased JsArray.
   * @return The original JsArray.
   */
  def decode(aliasArray: JsArray): Future[JsArray]
}

class JsonAliasModule(
  environment: Environment,
  configuration: Configuration) extends AbstractModule {
  def configure() = {
    val isEnabledJsonAlias: Boolean =
          configuration.getBoolean("ms.module.json.alias.enabled")
            .getOrElse(true)
    if (isEnabledJsonAlias) {
      val typeOfJsonAliasOpt: Option[String] =
            configuration.getString(
              "ms.module.json.alias.mode",
              Some(Set("local", "file", "redis", "dynamo")))
      typeOfJsonAliasOpt match {
        case Some("local") => {
          bind(classOf[JsonAlias]).
            to(classOf[JsonAliasLocal]).
            asEagerSingleton
          Logger.info("Bind JsonAlias to Local-based module.")
        }
        case Some("file") => {
          bind(classOf[JsonAlias]).
            to(classOf[JsonAliasLocal]).
            asEagerSingleton
          Logger.info("Bind JsonAlias to File-based module.")
        }
        case Some("redis") => {
          bind(classOf[JsonAlias]).
            to(classOf[JsonAliasRedis]).
            asEagerSingleton
          Logger.info("Bind JsonAlias to Redis-based module.")
        }
        case Some("dynamo") => {
          bind(classOf[JsonAlias]).
            to(classOf[JsonAliasDynamo]).
            asEagerSingleton
          Logger.info("Bind JsonAlias to Dynamo-based module.")
        }
        case _ => {
          Logger.warn("None valid json module can be binded.")
        }
      }
    }
  }
}
