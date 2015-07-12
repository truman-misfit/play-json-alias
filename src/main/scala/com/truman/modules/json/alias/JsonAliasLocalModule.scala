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
class JsonAliasLocal @Inject()(lifecycle: ApplicationLifecycle) extends JsonAlias {
  // release storage manager for alias store
  lifecycle.addStopHook { () =>
    Future.successful(Unit)
  }

  private val aliasMutex = new Object
  private var counter = 1000
  private lazy val src2alias = scala.collection.mutable.Map[String, String]()
  private lazy val alias2src = scala.collection.mutable.Map[String, String]()

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
      // Alloc a empty JsObject as buffer
      var encodedJsObject = JsObject(Seq())

      // iterate all attributes of the JSON
      jsObject.fieldSet.
        foreach { pair =>
          val jsonAttribute = pair._1
          val jsonValue = pair._2
          if (src2alias.contains(jsonAttribute)) {
            // attribute has been cached
            val shortAttribute = src2alias.get(jsonAttribute).get
            encodedJsObject = encodedJsObject + (shortAttribute, jsonValue)
          } else {
            // allocate a new short attribute for the new one
            // get a new counter for the new attribute
            aliasMutex.synchronized {
              // get new counter number
              counter = counter + 1
              val shortAttribute = Base62Encoder.encode(BigInt(counter))
              src2alias += ((jsonAttribute, shortAttribute))
              alias2src += ((shortAttribute, jsonAttribute))
              encodedJsObject = encodedJsObject + (shortAttribute, jsonValue)
            }
          }
        }

      Future.successful(encodedJsObject)
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

      var decodedJsObject = JsObject(Seq())

      // iterate all attributes of the JSON
      aliasObject.fieldSet.
        foreach { pair =>
          val jsonAttribute = pair._1
          val jsonValue = pair._2
          if (alias2src.contains(jsonAttribute)) {
            // attribute has been cached
            val decodedAttribute = alias2src.get(jsonAttribute).get
            decodedJsObject = decodedJsObject + (decodedAttribute, jsonValue)
          } else {
            // literally an Exception should be throw out
            Future.failed(new IllegalArgumentException(
              "Decoded JSON has invalid alias: " + jsonAttribute))
          }
      }

      Future.successful(decodedJsObject)
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
