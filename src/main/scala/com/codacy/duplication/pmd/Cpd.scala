package com.codacy.duplication.pmd

import java.io.{ByteArrayOutputStream, OutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}
import java.util

import better.files.File
import codacy.docker.api.duplication._
import codacy.docker.api.{DuplicationConfiguration, Source}
import com.codacy.api.dtos.{Language, Languages}
import net.sourceforge._
import net.sourceforge.pmd.cpd
import net.sourceforge.pmd.cpd.Match
import play.api.libs.json.{JsNumber, JsValue}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Cpd extends DuplicationTool {

  private val allLanguages: List[Language] = List(Languages.Python,
                                                  Languages.Ruby,
                                                  Languages.Java,
                                                  Languages.Javascript,
                                                  Languages.Scala,
                                                  Languages.CSharp)

  override def apply(path: Source.Directory,
                     language: Option[Language],
                     options: Map[DuplicationConfiguration.Key,
                                  DuplicationConfiguration.Value])
    : Try[List[DuplicationClone]] = {
    val baos = new ByteArrayOutputStream()
    val stdErr = System.err

    System.setErr(new PrintStream(baos, true, "utf-8"))
    val directoryPath: Path = (File.currentWorkingDirectory / path.path).path

    getLanguages(language).map { languages =>
      runCPD(directoryPath, languages, options)(stdErr)
    } match {
      case Success(matches) => Success(matches)
      case Failure(e) =>
        val errString = new String(baos.toByteArray, StandardCharsets.UTF_8)
        val msg =
          s"""|Failed to execute duplication: ${e.getMessage}
              |std:
              |$errString
         """.stripMargin

        Failure(new Exception(msg, e))
    }
  }

  private def runCPD(directory: Path,
                     languages: List[(pmd.cpd.Language, Int)],
                     options: Map[DuplicationConfiguration.Key,
                                  DuplicationConfiguration.Value])(
      errStream: PrintStream): List[DuplicationClone] = {

    languages.flatMap { lang =>
      val configuration = getConfiguration(lang, options)
      runWithConfiguration(configuration, directory)(errStream)
    }

  }

  private def runWithConfiguration(
      config: pmd.cpd.CPDConfiguration,
      directory: Path)(errStream: PrintStream): List[DuplicationClone] = {
    val cpd = new pmd.cpd.CPD(config)
    cpd.addRecursively(directory.toFile)

    System.setErr(errStream)

    cpd.go()

    val res: util.Iterator[Match] = cpd.getMatches
    res.asScala.map(matchToClone(_, directory)).toList
  }

  private def getLanguages(
      language: Option[Language]): Try[List[(pmd.cpd.Language, Int)]] = {

    val langs: Option[Try[List[(cpd.Language, Int)]]] =
      language.map {
        convertLanguage(_) match {
          case Some(x) => Success(List(x))
          case None =>
            Failure(
              new Throwable("Cpd.getLanguages: there is no language with key "))
        }
      }

    langs.getOrElse(Success(allLanguages.flatMap(convertLanguage)))
  }

  private def convertLanguage(
      language: Language): Option[(pmd.cpd.Language, Int)] = {
    language match {
      case Languages.Python     => Some((new pmd.cpd.PythonLanguage, 50))
      case Languages.Ruby       => Some((new pmd.cpd.RubyLanguage, 50))
      case Languages.Java       => Some((new pmd.cpd.JavaLanguage, 100))
      case Languages.Javascript => Some((new pmd.cpd.EcmascriptLanguage, 40))
      case Languages.Scala =>
        val cpdScala = new pmd.cpd.ScalaLanguage
        object ScalaL
            extends pmd.cpd.AbstractLanguage(cpdScala.getName,
                                             cpdScala.getTerseName,
                                             ScalaTokenizer,
                                             cpdScala.getExtensions.asScala: _*)
        Some((ScalaL, 50))
      case Languages.CSharp => Some((new pmd.cpd.CsLanguage, 50))
      case _                => None
    }
  }

  private def getConfiguration(language: (pmd.cpd.Language, Int),
                               options: Map[DuplicationConfiguration.Key,
                                            DuplicationConfiguration.Value]) = {

    val ignoreAnnotations = true
    val skipLexicalErrors = true

    val (cpdLanguage, defaultMinToken) = language

    val minTokenMatch: Int = options
      .get(DuplicationConfiguration.Key("minTokenMatch"))
      .fold(defaultMinToken) { value: DuplicationConfiguration.Value =>
        Option(value: JsValue)
          .collect {
            case JsNumber(number) => number.toInt
            case _                => defaultMinToken
          }
          .getOrElse(defaultMinToken)
      }

    val cfg = new pmd.cpd.CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    cfg.setIgnoreAnnotations(ignoreAnnotations)
    cfg.setSkipLexicalErrors(skipLexicalErrors)
    cfg.setMinimumTileSize(minTokenMatch)
    cfg
  }

  private def matchToClone(m: pmd.cpd.Match,
                           rootDirectory: Path): DuplicationClone = {
    val files: List[DuplicationCloneFile] = m.getMarkSet.asScala.map { mark =>
      val file = rootDirectory.relativize(Paths.get(mark.getFilename))
      DuplicationCloneFile(file.toString, mark.getBeginLine, mark.getEndLine)
    }(collection.breakOut)

    DuplicationClone(m.getSourceCodeSlice,
                     m.getTokenCount,
                     m.getLineCount,
                     files)
  }

}

object NullPrintStream
    extends PrintStream(new OutputStream {
      override def write(b: Int): Unit = ()
    })
