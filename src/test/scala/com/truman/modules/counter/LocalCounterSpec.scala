package com.truman.modules.counter

import play.api._
import play.api.test._
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._

import javax.inject._
import scala.concurrent.ExecutionContext.Implicits.global

class LocalCounterSpec extends PlaySpecification {

  "A general counter specification" should {
    "local counter demon" in new WithApplication {
      val counter = app.injector.instanceOf[Counter]
      val one = Await.result(counter.next, 10.millis)
      val two = Await.result(counter.next, 10.millis)
      one + 1 must_== two
    }
  }
}
