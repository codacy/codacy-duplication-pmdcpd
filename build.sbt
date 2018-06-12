import sbt.Keys._
import sbt._

name := """codacy-duplication-pmdcpd"""

version := "1.0.0-SNAPSHOT"

val scalaBinaryVersionNumber = "2.12"
val scalaVersionNumber = s"$scalaBinaryVersionNumber.4"

scalaVersion := scalaVersionNumber
scalaVersion in ThisBuild := scalaVersionNumber
scalaBinaryVersion in ThisBuild := scalaBinaryVersionNumber

scapegoatVersion in ThisBuild := "1.3.5"

lazy val codacyDuplictionPmdCpd = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    inThisBuild(
      List(
        organization := "com.codacy",
        scalaVersion := scalaVersionNumber,
        version := "0.1.0-SNAPSHOT",
        scalacOptions ++= Common.compilerFlags,
        scalacOptions in Test ++= Seq("-Yrangepos"),
        scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"))),
    name := "codacy-duplication-pmdcpd",
    // App Dependencies
    libraryDependencies ++= Seq(
      Dependencies.Codacy.duplicationSeed withSources(),
      Dependencies.playJson,
      Dependencies.scalaMeta,
      Dependencies.csPmd,
      Dependencies.javaPmd,
      Dependencies.javascriptPmd,
      Dependencies.pythonPmd,
      Dependencies.rubyPmd,
      Dependencies.scalaPmd),
    // Test Dependencies
    libraryDependencies ++= Seq(Dependencies.specs2).map(_ % Test))
  .settings(Common.dockerSettings: _*)
