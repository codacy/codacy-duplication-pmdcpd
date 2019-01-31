import sbt._

object Dependencies {

  object Codacy {
    val duplicationSeed = "com.codacy" %% "codacy-duplication-scala-seed" % "2.0.0-pre.151"
  }

  val playJson = "com.typesafe.play" %% "play-json" % "2.6.9"

  val scalaMeta = "org.scalameta" %% "scalameta" % "1.4.0"

  private val pmdGroupArtifactID = "net.sourceforge.pmd"
  private val pmdVersion = "6.11.0"
  private val scalaPmd = pmdGroupArtifactID % "pmd-scala" % pmdVersion
  private val javaPmd = pmdGroupArtifactID % "pmd-java" % pmdVersion
  private val javascriptPmd = pmdGroupArtifactID % "pmd-javascript" % pmdVersion
  private val rubyPmd = pmdGroupArtifactID % "pmd-ruby" % pmdVersion
  private val pythonPmd = pmdGroupArtifactID % "pmd-python" % pmdVersion
  private val csPmd = pmdGroupArtifactID % "pmd-cs" % pmdVersion
  private val cppPmd = pmdGroupArtifactID % "pmd-cpp" % pmdVersion
  private val goPmd = pmdGroupArtifactID % "pmd-go" % pmdVersion
  private val plsqlPmd = pmdGroupArtifactID % "pmd-plsql" % pmdVersion
  private val swiftPmd = pmdGroupArtifactID % "pmd-swift" % pmdVersion
  val pmdLanguages = Seq(csPmd, cppPmd, javascriptPmd, goPmd, javaPmd, plsqlPmd, pythonPmd, rubyPmd, scalaPmd, swiftPmd)

  val specs2Version = "4.2.0"
  val specs2 = "org.specs2" %% "specs2-core" % specs2Version

}
