import sbt._

object Dependencies {

  val playJson = "com.typesafe.play" %% "play-json" % "2.6.9"
  val duplicationScalaSeed = "com.codacy" %% "codacy-duplication-scala-seed" % "0.1.81"
  val scalaMeta = "org.scalameta" %% "scalameta" % "1.4.0"

  private val pmdGroupArtifactID = "net.sourceforge.pmd"
  private val pmdVersion = "6.0.1"
  val scalaPmd = pmdGroupArtifactID % "pmd-scala" % pmdVersion
  val javaPmd = pmdGroupArtifactID % "pmd-java" % pmdVersion
  val javascriptPmd = pmdGroupArtifactID % "pmd-javascript" % pmdVersion
  val rubyPmd = pmdGroupArtifactID % "pmd-ruby" % pmdVersion
  val pythonPmd = pmdGroupArtifactID % "pmd-python" % pmdVersion
  val csPmd = pmdGroupArtifactID % "pmd-cs" % pmdVersion

}
