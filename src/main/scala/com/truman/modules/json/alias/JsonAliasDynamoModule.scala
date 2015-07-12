package com.truman.modules.json.alias

import javax.inject.{Inject, Singleton}

import scala.util.Try
import scala.concurrent.Future
import com.google.inject.AbstractModule

import play.api.Play
import play.api.libs.json._
import play.api.inject.ApplicationLifecycle

import com.truman.utils.Base62Encoder

@Singleton
class JsonAliasDynamo @Inject()(lifecycle: ApplicationLifecycle) extends JsonAlias {
  // release storage manager for alias store
  lifecycle.addStopHook { () =>
    Future.successful(Unit)
  }

  private def validate(raw: JsValue): Boolean = {
    Try(Some(raw)).map { raw =>
      1 == 1 // alway valid
    }.getOrElse(false)
  }

  override def encode(jsObject: JsObject): Future[JsObject] = {
    if (!validate(jsObject)) {
      Future.failed(new IllegalArgumentException("Raw JSON is invalid."))
    } else {
      // alias start
      // iterate all attributes of the JSON
      Future.successful(jsObject)
    }
  }

  override def encode(jsArray: JsArray): Future[JsArray] = {
    if (!validate(jsArray)) {
      Future.failed(new IllegalArgumentException("Raw JSON is invalid."))
    } else {
      // alias start
      // iterate all attributes of the JSON
      Future.successful(jsArray)
    }
  }

  override def decode(aliasObject: JsObject): Future[JsObject] = {
    if (!validate(aliasObject)) {
      Future.failed(new IllegalArgumentException("Raw JSON is invalid."))
    } else {
      // alias start
      // iterate all JsObject within JsArray
      Future.successful(aliasObject)
    }
  }

  override def decode(aliasArray: JsArray): Future[JsArray] = {
    if (!validate(aliasArray)) {
      Future.failed(new IllegalArgumentException("Raw JSON is invalid."))
    } else {
      // alias start
      // iterate all JsObject within JsArray
      Future.successful(aliasArray)
    }
  }

}
