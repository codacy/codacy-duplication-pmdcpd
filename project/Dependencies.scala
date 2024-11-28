import sbt._

object Dependencies {

  object Codacy {
    val duplicationSeed = "com.codacy" %% "codacy-duplication-scala-seed" % "2.1.1"
  }

  val playJson = "org.playframework" %% "play-json" % "3.0.4"

  val scalaMeta = "org.scalameta" %% "scalameta" % "4.12.0"

  private val pmdVersion = "7.7.0"

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
