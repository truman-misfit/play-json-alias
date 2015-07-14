package com.truman.modules.counter

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import com.google.inject.AbstractModule

import play.api.Play
import play.api.Play.current
import play.api.libs.json._
import play.api.inject.ApplicationLifecycle

@Singleton
class LocalCounterModule @Inject()(lifecycle: ApplicationLifecycle) extends Counter {
  lifecycle.addStopHook { () =>
    Future.successful(Unit)
  }

  lazy val token = Play.application.configuration.getLong("application.counter.token").get

  // The rolling counter
  private var roller = -1

  override def next: Future[BigInt] = {
    nextInternal
  }

  private def nextInternal: Future[BigInt] = {
    val nextRollingValue = this.synchronized[Long] {
      roller = roller + 1
      if (roller > 256) {
        throw new Exception("Out Of Counter Boundary.")
      } else {
        roller
      }
    }
    Future.successful(
      BigInt(
        nextRollingValue +
        (256 * token)))
  }
}
