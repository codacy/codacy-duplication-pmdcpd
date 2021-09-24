ThisBuild / scalaVersion := "2.13.6"

lazy val codacyDuplictionPmdCpd = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "codacy-duplication-pmdcpd",
    // Materializing code in memory with `getSourceCodeSlice` bomb the heap with Strings
    Universal / javaOptions ++= Seq("-XX:+UseG1GC", "-XX:+UseStringDeduplication"),
    // App Dependencies
    libraryDependencies ++= Seq(Dependencies.Codacy.duplicationSeed, Dependencies.playJson, Dependencies.scalaMeta) ++
      Dependencies.pmdLanguages,
    // Test Dependencies
    libraryDependencies += Dependencies.specs2 % Test)
