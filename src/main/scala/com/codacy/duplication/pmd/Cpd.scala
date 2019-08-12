package com.codacy.duplication.pmd

import _root_.java.io.{ByteArrayOutputStream, PrintStream}
import _root_.java.nio.charset.StandardCharsets
import _root_.java.nio.file.{Path, Paths}

import better.files.File
import com.codacy.docker.api.duplication._
import com.codacy.plugins.api.duplication.{DuplicationClone, DuplicationCloneFile, DuplicationTool}
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.{Options, Source}
import net.sourceforge.pmd.cpd.{Language => CPDLanguage, _}

import _root_.scala.collection.JavaConverters._
import _root_.scala.util.{Failure, Success, Try}

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

  private val ignoreAnnotationsKey = Options.Key("ignoreAnnotations")
  private val skipLexicalErrorsKey = Options.Key("skipLexicalErrors")
  private val minimumTileSizeKey = Options.Key("minTokenMatch")
  private val ignoreIdentifiersKey = Options.Key("ignoreIdentifiers")
  private val ignoreLiteralsKey = Options.Key("ignoreLiterals")
  private val ignoreUsingsKey = Options.Key("ignoreUsings")

  override def apply(path: Source.Directory,
                     language: Option[Language],
                     options: Map[Options.Key, Options.Value]): Try[List[DuplicationClone]] = {

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

  def runWithConfiguration(config: CPDConfiguration, directory: Path): List[DuplicationClone] = {
    val cpd = new CPD(config)
    cpd.addRecursively(directory.toFile)
    cpd.go()
    val x = cpd.getMatches.asScala.toList
    // val matches = cpd.getMatches.asScala
    // println(matches.size)
    x.map { x =>
      println("X -> " + x)
      duplicationClone(x, directory)
    }
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

  private def resolveConfigurations(languages: List[Language],
                                    options: Map[Options.Key, Options.Value]): List[CPDConfiguration] = {

    languages.flatMap(resolveConfiguration(_, options))
  }

  private def resolveConfiguration(language: Language,
                                   options: Map[Options.Key, Options.Value]): Option[CPDConfiguration] = {
    language match {
      case Languages.CSharp => Some(cpdConfiguration(new CsLanguage, 50, options))
      case Languages.C | Languages.CPP =>
        val language = new CPPLanguage()
        // TODO: This workaround can be removed after 6.7.0
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

  private def cpdConfiguration(cpdLanguage: CPDLanguage,
                               defaultMinToken: Int,
                               options: Map[Options.Key, Options.Value]): CPDConfiguration = {
    val cfg = new CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    cfg.setIgnoreAnnotations(options.getValue(ignoreAnnotationsKey, true))
    cfg.setSkipLexicalErrors(options.getValue(skipLexicalErrorsKey, true))
    cfg.setMinimumTileSize(options.getValue(minimumTileSizeKey, defaultMinToken))
    cfg.setIgnoreIdentifiers(options.getValue(ignoreIdentifiersKey, true))
    cfg.setIgnoreLiterals(options.getValue(ignoreLiteralsKey, true))
    cfg.setIgnoreUsings(options.getValue(ignoreUsingsKey, true))
    cfg
  }

  private def duplicationClone(m: Match, rootDirectory: Path): DuplicationClone = {
    // println("da qui")
    println("match " + m)
    val markset = m.getMarkSet.asScala.toList
    val files: List[DuplicationCloneFile] = markset.map { mark =>
      println("debug mark -> " + mark)

      println(mark.getBeginLine)
      println(mark.getEndLine)

      val file = rootDirectory.relativize(Paths.get(mark.getFilename))
      DuplicationCloneFile(file.toString, mark.getBeginLine, mark.getEndLine)
    }(collection.breakOut)

    // println("files -> "+files.size)
    DuplicationClone(m.getSourceCodeSlice, m.getTokenCount, m.getLineCount, files)
  }

}
