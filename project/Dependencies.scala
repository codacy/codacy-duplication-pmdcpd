import sbt._

object Dependencies {

  object Codacy {
    val duplicationSeed = "com.codacy" %% "codacy-duplication-scala-seed" % "2.0.1"
  }

  val playJson = "com.typesafe.play" %% "play-json" % "2.7.4"

  val scalaMeta = "org.scalameta" %% "scalameta" % "4.4.28"

  private val pmdVersion = "6.55.0"

  val pmdLanguages = Seq(
    "scala",
    "java",
    "javascript",
    "ruby",
    "python",
    "cs",
    "cpp",
    "go",
    "plsql",
    "swift").map {
    case "scala" => "net.sourceforge.pmd" %% "pmd-scala" % pmdVersion
    case language => "net.sourceforge.pmd" % s"pmd-$language" % pmdVersion
  }
  val specs2Version = "4.12.12"
  val specs2 = "org.specs2" %% "specs2-core" % specs2Version

}
