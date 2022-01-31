package com.codacy.docker.api

import com.codacy.plugins.api.Options
import play.api.libs.json.{JsBoolean, JsNumber, JsValue}

import scala.language.implicitConversions

package object duplication {

  implicit def boolean(value: JsValue): Option[Boolean] =
    Option(value: JsValue).collect { case JsBoolean(bool) => bool }

  implicit def int(value: JsValue): Option[Int] =
    Option(value: JsValue).collect { case JsNumber(bigDecimal) => bigDecimal.toInt }

  implicit class DuplicationConfigurationExtended(options: Map[Options.Key, Options.Value]) {

    def getValue[A](key: String)(implicit ev: JsValue => Option[A]): Option[A] = {
      options.get(Options.Key(key)).flatMap { value: Options.Value =>
        Option(value: JsValue).flatMap(ev)
      }
    }
  }

}
