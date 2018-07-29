package com.codacy.duplication.pmd

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import better.files.File
import codacy.docker.api.duplication._
import codacy.docker.api.{DuplicationConfiguration, Source}
import com.codacy.api.dtos.{Language, Languages}
import net.sourceforge.pmd.cpd.{Language => CPDLanguage, _}
import play.api.libs.json.{JsBoolean, JsNumber, JsValue}
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Cpd extends DuplicationTool {

  private val allLanguages: List[Language] =
    List[Language](
      Languages.CSharp,
      Languages.C,
      Languages.CPP,
      Languages.Javascript,
      Languages.Go,
      Languages.Java,
      Languages.SQL,
      Languages.Python,
      Languages.Ruby,
      Languages.Scala,
      Languages.Swift)

  override def apply(
    path: Source.Directory,
    language: Option[Language],
    options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]): Try[List[DuplicationClone]] = {

    val baos = new ByteArrayOutputStream()
    val stdErr = System.err
    System.setErr(new PrintStream(baos, true, "utf-8"))

    val directoryPath: Path = (File.currentWorkingDirectory / path.path).path

    val cpdResultsTry = resolveLanguages(language).map { languages =>
      resolveConfigurations(languages, options).flatMap(runWithConfiguration(_, directoryPath))
    }

    System.setErr(stdErr)

    cpdResultsTry match {
      case Failure(e) =>
        val errString = new String(baos.toByteArray, StandardCharsets.UTF_8)
        val msg =
          s"""|Failed to execute duplication: ${e.getMessage}
              |std:
              |$errString
         """.stripMargin
        Failure(new Exception(msg, e))
      case Success(results) => Success(results)
    }
  }

  private def runWithConfiguration(config: CPDConfiguration, directory: Path): List[DuplicationClone] = {
    val cpd = new CPD(config)
    cpd.addRecursively(directory.toFile)
    cpd.go()
    cpd.getMatches.asScala.map(duplicationClone(_, directory)).toList
  }

  private def resolveLanguages(language: Option[Language]): Try[List[Language]] = {
    language match {
      case Some(lang) =>
        if (allLanguages.contains(lang)) {
          Success(List(lang))
        } else {
          val message = s"$lang language is not supported"
          Failure(new Exception(message))
        }
      case None => Success(allLanguages)
    }
  }

  private def resolveConfigurations(
    languages: List[Language],
    options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]): List[CPDConfiguration] = {

    languages.flatMap(resolveConfiguration(_, options))
  }

  private def resolveConfiguration(
    language: Language,
    options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]): Option[CPDConfiguration] = {
    language match {
      case Languages.CSharp => Some(cpdConfiguration(new CsLanguage, 50, options))
      case Languages.C | Languages.CPP =>
        val language = new CPPLanguage()
        language.setProperties(System.getProperties)
        Some(cpdConfiguration(language, 50, options))
      case Languages.Javascript => Some(cpdConfiguration(new EcmascriptLanguage, 40, options))
      case Languages.Go         => Some(cpdConfiguration(new GoLanguage, 40, options))
      case Languages.Java       => Some(cpdConfiguration(new JavaLanguage, 100, options))
      case Languages.SQL        => Some(cpdConfiguration(new PLSQLLanguage, 100, options))
      case Languages.Python     => Some(cpdConfiguration(new PythonLanguage, 50, options))
      case Languages.Ruby       => Some(cpdConfiguration(new RubyLanguage, 50, options))
      case Languages.Swift      => Some(cpdConfiguration(new SwiftLanguage, 50, options))
      case Languages.Scala =>
        val cpdScala = new ScalaLanguage
        val scalaLanguage = new AbstractLanguage(
          cpdScala.getName,
          cpdScala.getTerseName,
          ScalaTokenizer,
          cpdScala.getExtensions.asScala: _*) {}
        Some(cpdConfiguration(scalaLanguage, 50, options))
      case _ => None
    }
  }

  // TODO: Move to codacy-plugins-api
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

  private def cpdConfiguration(cpdLanguage: CPDLanguage,
                               defaultMinToken: Int,
                               options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]) = {
    val cfg = new CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    cfg.setIgnoreAnnotations(options.getValue[Boolean](DuplicationConfiguration.Key("ignoreAnnotations"), true))
    cfg.setSkipLexicalErrors(options.getValue[Boolean](DuplicationConfiguration.Key("skipLexicalErrors"), true))
    cfg.setMinimumTileSize(options.getValue[Int](DuplicationConfiguration.Key("minTokenMatch"), defaultMinToken))
    cfg.setIgnoreIdentifiers(options.getValue[Boolean](DuplicationConfiguration.Key("ignoreIdentifiers"), true))
    cfg.setIgnoreLiterals(options.getValue[Boolean](DuplicationConfiguration.Key("ignoreLiterals"), true))
    cfg.setIgnoreUsings(options.getValue[Boolean](DuplicationConfiguration.Key("ignoreUsings"), true))
    cfg
  }

  private def duplicationClone(m: Match, rootDirectory: Path): DuplicationClone = {
    val files: List[DuplicationCloneFile] = m.getMarkSet.asScala.map { mark =>
      val file = rootDirectory.relativize(Paths.get(mark.getFilename))
      DuplicationCloneFile(file.toString, mark.getBeginLine, mark.getEndLine)
    }(collection.breakOut)

    DuplicationClone(m.getSourceCodeSlice, m.getTokenCount, m.getLineCount, files)
  }

}
