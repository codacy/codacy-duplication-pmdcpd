package com.codacy.docker.api

import play.api.libs.json.{JsBoolean, JsNumber, JsValue}

package object duplication {

  import codacy.docker.api._
  import codacy.docker.api.duplication._

  implicit def boolean(value: JsValue): Option[Boolean] =
    Option(value: JsValue).collect { case JsBoolean(bool) => bool }

  implicit def int(value: JsValue): Option[Int] =
    Option(value: JsValue).collect { case JsNumber(bigDecimal) => bigDecimal.toInt }

  implicit class DuplicationConfigurationExtended(
    options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]) {

    def getValue[A](key: DuplicationConfiguration.Key, defaultValue: A)(implicit ev: JsValue => Option[A]): A = {
      options.get(key).fold(defaultValue) { value: DuplicationConfiguration.Value =>
        Option(value: JsValue).flatMap(ev).getOrElse(defaultValue)
      }
    }
  }

}
