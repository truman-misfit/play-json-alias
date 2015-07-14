package com.truman.modules.counter

import scala.concurrent.Future
import com.google.inject.AbstractModule

import play.api.libs.json._
import play.api.{ Logger, Environment, Configuration }

/**
 * Service that generates a unique sequence of BigInt values
 */
trait Counter {
  /**
   * Get the next value from the counter
   */
  def next: Future[BigInt]
}

class CounterModule(
  environment: Environment,
  configuration: Configuration) extends AbstractModule {
  def configure() = {
    val isEnabledCounter: Boolean =
          configuration.getBoolean("ms.module.counter.enabled")
            .getOrElse(true)
    if (isEnabledCounter) {
      val typeOfCounterOpt: Option[String] =
            configuration.getString(
              "ms.module.counter.mode",
              Some(Set("local", "redis")))
      typeOfCounterOpt match {
        case Some("local") => {
          bind(classOf[Counter]).
            to(classOf[LocalCounterModule]).
            asEagerSingleton
          Logger.info("Bind Counter to local generator.")
        }
        case Some("redis") => {
          bind(classOf[Counter]).
            to(classOf[RedisCounterModule]).
            asEagerSingleton
          Logger.info("Bind Counter to Redis-based generator.")
        }
        case _ => {
          Logger.warn("None valid counter module can be binded.")
        }
      }
    }
  }
}
