package com.truman.modules.json.alias

import javax.inject.{Inject, Singleton}

import scala.util.Try
import scala.util.{Success, Failure}
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import com.google.inject.AbstractModule

import play.api.Play
import play.api.libs.json._
import play.api.inject.ApplicationLifecycle

import com.truman.utils.Base62Encoder
import com.truman.modules.counter.Counter
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class JsonAliasLocal @Inject()(
  lifecycle: ApplicationLifecycle, counter: Counter) extends JsonAlias {
  // release storage manager for alias store
  lifecycle.addStopHook { () =>
    Future.successful(Unit)
  }

  private val aliasMutex = new Object
  private lazy val src2alias = scala.collection.mutable.Map[String, String]()
  private lazy val alias2src = scala.collection.mutable.Map[String, String]()

  private def validate(raw: JsValue): Boolean = {
    Try(Some(raw)).map { raw =>
      true // alway valid
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
          var recurEncodedJsonValue = jsonValue
          // recursive encoding for jsonValue
          jsonValue.validate[JsObject] match {
            case s: JsSuccess[JsObject] => {
              recurEncodedJsonValue = Await.
                result(encode(s.get), 10.millis)
            }
            case e: JsError => {
              jsonValue.validate[JsArray] match {
                case s: JsSuccess[JsArray] => {
                  recurEncodedJsonValue = Await.
                    result(encode(s.get), 10.millis)
                }
                case e: JsError => {
                  // do nothing
                }
              }
            }
          }

          // encode current JSON key
          if (src2alias.contains(jsonAttribute)) {
            // attribute has been cached
            val shortAttribute = src2alias.get(jsonAttribute).get
            encodedJsObject = encodedJsObject + (
              shortAttribute, recurEncodedJsonValue)
          } else {
            // allocate a new short attribute for the new one
            aliasMutex.synchronized {
              // get new counter number
              val currentCounter = Await.result(counter.next, 10.millis)
              val shortAttribute = Base62Encoder.encode(currentCounter)
              src2alias += ((jsonAttribute, shortAttribute))
              alias2src += ((shortAttribute, jsonAttribute))
              encodedJsObject = encodedJsObject + (
                shortAttribute, recurEncodedJsonValue)
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
      var encodedJsArray = JsArray(Seq())

      jsArray.value.foreach { jsValue =>
        jsValue.validate[JsObject] match {
          case s: JsSuccess[JsObject] => {
            val validatedJsObject = s.get
            val encodedJsObjectWithinJsArray = Await.
                  result(encode(validatedJsObject), 10.millis)
            encodedJsArray = encodedJsArray :+ encodedJsObjectWithinJsArray
          }
          case e: JsError => {
            // literally we should handle someother type,
            // for example: JsArray
            jsValue.validate[JsArray] match {
              case s: JsSuccess[JsArray] => {
                val validatedJsArray = s.get
                val encodedJsArrayWithinJsArray = Await.
                      result(encode(validatedJsArray), 10.millis)
                encodedJsArray = encodedJsArray :+ encodedJsArrayWithinJsArray
              }
              case e: JsError => {
                // do nothing
                encodedJsArray = encodedJsArray :+ jsValue
              }
            }
          }
        }
      }

      Future.successful(encodedJsArray)
    }
  }

  override def decode(aliasObject: JsObject): Future[JsObject] = {
    if (!validate(aliasObject)) {
      Future.failed(new IllegalArgumentException("Raw JSON is invalid."))
    } else {
      // alias start
      var decodedJsObject = JsObject(Seq())

      // iterate all attributes of the JSON
      aliasObject.fieldSet.
        foreach { pair =>
          val jsonAttribute = pair._1
          val jsonValue = pair._2
          // recursively parse the json value
          var recurDecodedJsonValue = jsonValue
          // recursive encoding for jsonValue
          jsonValue.validate[JsObject] match {
            case s: JsSuccess[JsObject] => {
              recurDecodedJsonValue = Await.
                result(decode(s.get), 10.millis)
            }
            case e: JsError => {
              jsonValue.validate[JsArray] match {
                case s: JsSuccess[JsArray] => {
                  recurDecodedJsonValue = Await.
                    result(decode(s.get), 10.millis)
                }
                case e: JsError => {
                  // do nothing
                }
              }
            }
          }

          // decode the alias to original attribute key
          if (alias2src.contains(jsonAttribute)) {
            // attribute has been cached
            val decodedAttribute = alias2src.get(jsonAttribute).get
            decodedJsObject = decodedJsObject + (decodedAttribute, recurDecodedJsonValue)
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
      var decodedJsArray = JsArray(Seq())
      // iterate all JsObject within JsArray
      aliasArray.value.foreach { jsValue =>
        jsValue.validate[JsObject] match {
          case s: JsSuccess[JsObject] => {
            val validatedJsObject = s.get
            val decodedJsObjectWithinJsArray = Await.
                  result(decode(validatedJsObject), 10.millis)
            decodedJsArray = decodedJsArray :+ decodedJsObjectWithinJsArray
          }
          case e: JsError => {
            jsValue.validate[JsArray] match {
              case s: JsSuccess[JsArray] => {
                val validatedJsObject = s.get
                val decodedJsArrayWithinJsArray = Await.
                      result(decode(validatedJsObject), 10.millis)
                decodedJsArray = decodedJsArray :+ decodedJsArrayWithinJsArray
              }
              case e: JsError => {
                // do nothing
              }
            }
          }
        }
      }

      Future.successful(decodedJsArray)
    }
  }

}
