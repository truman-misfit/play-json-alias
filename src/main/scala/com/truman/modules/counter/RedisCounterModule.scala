package com.truman.modules.counter

import javax.inject.{Inject, Singleton}

import scala.concurrent.Future
import com.google.inject.AbstractModule

import play.api.Play
import play.api.libs.json._
import play.api.inject.ApplicationLifecycle

@Singleton
class RedisCounterModule @Inject()(lifecycle: ApplicationLifecycle) extends Counter {
  lifecycle.addStopHook { () =>
    Future.successful(Unit)
  }

  override def next: Future[BigInt] = {
    Future.successful(BigInt(1))
  }
}
