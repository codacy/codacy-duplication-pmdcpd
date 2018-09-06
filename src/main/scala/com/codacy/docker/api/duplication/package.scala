package com.codacy.docker.api

import com.codacy.plugins.api.Options
import play.api.libs.json.{JsBoolean, JsNumber, JsValue}

package object duplication {

  implicit def boolean(value: JsValue): Option[Boolean] =
    Option(value: JsValue).collect { case JsBoolean(bool) => bool }

  implicit def int(value: JsValue): Option[Int] =
    Option(value: JsValue).collect { case JsNumber(bigDecimal) => bigDecimal.toInt }

  implicit class DuplicationConfigurationExtended(options: Map[Options.Key, Options.Value]) {

    def getValue[A](key: Options.Key, defaultValue: A)(implicit ev: JsValue => Option[A]): A = {
      options.get(key).fold(defaultValue) { value: Options.Value =>
        Option(value: JsValue).flatMap(ev).getOrElse(defaultValue)
      }
    }
  }

}
