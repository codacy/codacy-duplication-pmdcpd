import com.typesafe.sbt.packager.docker.Cmd
import Dependencies._

name := """codacy-duplication-pmdcpd"""

version := "1.0.0-SNAPSHOT"

val scalaBinaryVersionNumber = "2.12"
val languageVersion = s"$scalaBinaryVersionNumber.4"

scalaVersion := languageVersion

resolvers ++= Seq(
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  playJson withSources(),
  duplicationScalaSeed withSources(),
  scalaPmd withSources(),
  javaPmd withSources(),
  javascriptPmd withSources(),
  rubyPmd withSources(),
  pythonPmd withSources(),
  csPmd withSources(),
  scalaMeta withSources()
)

// FIXES: package database contains object and package with same name: DBType
scalacOptions := scalacOptions.value.filterNot(_ == "-Xfatal-warnings") ++ Seq("-Yresolve-term-conflict:object")

enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

mappings in Universal <++= (resourceDirectory in Compile) map { (resourceDir: File) =>
  val src = resourceDir / "docs"
  val dest = "/docs"

  for {
    path <- (src ***).get
    if !path.isDirectory
  } yield path -> path.toString.replaceFirst(src.toString, dest)
}

val dockerUser = "docker"
val dockerGroup = "docker"

daemonUser in Docker := dockerUser

daemonGroup in Docker := dockerGroup

dockerBaseImage := "develar/java"

val installAll = """apk update && apk add bash curl &&
                   |rm -rf /tmp/* &&
                   |rm -rf /var/cache/apk/*""".stripMargin.replaceAll(System.lineSeparator(), " ")

dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("WORKDIR", _) => List(cmd,
    Cmd("RUN", installAll)
  )

  case cmd@(Cmd("ADD", "opt /opt")) => List(cmd,
    Cmd("RUN", s"adduser -u 2004 -D $dockerUser")
  )
  case other => List(other)
}
