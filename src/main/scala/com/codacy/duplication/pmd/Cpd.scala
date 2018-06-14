package com.codacy.duplication.pmd

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import better.files.File
import codacy.docker.api.duplication._
import codacy.docker.api.{DuplicationConfiguration, Source}
import com.codacy.api.dtos.{Language, Languages}
import net.sourceforge.pmd.cpd.{Language => CPDLanguage, _}
import play.api.libs.json.{JsNumber, JsValue}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Cpd extends DuplicationTool {

  private val allLanguages: List[Language] =
    List[Language](
      Languages.Python,
      Languages.Ruby,
      Languages.Java,
      Languages.Javascript,
      Languages.Scala,
      Languages.CSharp)

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
      case Languages.Python     => Some(cpdConfiguration(new PythonLanguage, 50, options))
      case Languages.Ruby       => Some(cpdConfiguration(new RubyLanguage, 50, options))
      case Languages.Java       => Some(cpdConfiguration(new JavaLanguage, 100, options))
      case Languages.Javascript => Some(cpdConfiguration(new EcmascriptLanguage, 40, options))
      case Languages.Scala =>
        val cpdScala = new ScalaLanguage
        val scalaLanguage = new AbstractLanguage(
          cpdScala.getName,
          cpdScala.getTerseName,
          ScalaTokenizer,
          cpdScala.getExtensions.asScala: _*) {}
        Some(cpdConfiguration(scalaLanguage, 50, options))
      case Languages.CSharp => Some(cpdConfiguration(new CsLanguage, 50, options))
      case _                => None
    }
  }

  private def cpdConfiguration(cpdLanguage: CPDLanguage,
                               defaultMinToken: Int,
                               options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value]) = {

    val ignoreAnnotations = true
    val skipLexicalErrors = true

    val minTokenMatch: Int = options.get(DuplicationConfiguration.Key("minTokenMatch")).fold(defaultMinToken) {
      value: DuplicationConfiguration.Value =>
        Option(value: JsValue).collect {
          case JsNumber(number) => number.toInt
          case _                => defaultMinToken
        }.getOrElse(defaultMinToken)
    }

    val cfg = new CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    cfg.setIgnoreAnnotations(ignoreAnnotations)
    cfg.setSkipLexicalErrors(skipLexicalErrors)
    cfg.setMinimumTileSize(minTokenMatch)
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
