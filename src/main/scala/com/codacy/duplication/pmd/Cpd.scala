package com.codacy.duplication.pmd

import _root_.java.io.{ByteArrayOutputStream, PrintStream}
import _root_.java.nio.charset.StandardCharsets
import _root_.java.nio.file.{Path, Paths}
import _root_.java.util.Properties

import better.files._
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
      languages.flatMap { language =>
        val files = File(directoryPath)
          .walk()
          .filter(file => file.isRegularFile && language.extensions.contains(file.extension.getOrElse(file.name)))
          .toSeq

        val configuration = resolveConfiguration(language, options)
        runWithConfiguration(configuration, directoryPath, files)
      }
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

  private def runWithConfiguration(config: CPDConfiguration,
                                   directory: Path,
                                   files: Seq[File]): List[DuplicationClone] = {
    val cpd = new CPD(config)
    cpd.add(files.map(_.toJava).asJava)
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

  private def resolveConfiguration(language: Language, options: Map[Options.Key, Options.Value]): CPDConfiguration = {
    language match {
      case Languages.CSharp            => cpdConfiguration(new CsLanguage, 50, options)
      case Languages.C | Languages.CPP => cpdConfiguration(new CPPLanguage(), 50, options)
      case Languages.Javascript        => cpdConfiguration(new EcmascriptLanguage, 40, options)
      case Languages.Go                => cpdConfiguration(new GoLanguage, 40, options)
      case Languages.Java              => cpdConfiguration(new JavaLanguage, 100, options)
      case Languages.SQL               => cpdConfiguration(new PLSQLLanguage, 100, options)
      case Languages.Python            => cpdConfiguration(new PythonLanguage, 50, options)
      case Languages.Ruby              => cpdConfiguration(new RubyLanguage, 50, options)
      case Languages.Swift             => cpdConfiguration(new SwiftLanguage, 50, options)
      case Languages.Scala =>
        val cpdScala = new ScalaLanguage
        val scalaLanguage = new AbstractLanguage(
          cpdScala.getName,
          cpdScala.getTerseName,
          com.codacy.duplication.pmd.ScalaTokenizer,
          cpdScala.getExtensions.asScala.toSeq: _*) {}
        cpdConfiguration(scalaLanguage, 50, options)
      case other =>
        throw new Exception(s"$other Language not supported")
    }
  }

  private def cpdConfiguration(cpdLanguage: CPDLanguage,
                               defaultMinToken: Int,
                               options: Map[Options.Key, Options.Value]): CPDConfiguration = {
    val ignoreLiterals = options.getValue(ignoreLiteralsKey, true)
    val ignoreAnnotations = options.getValue(ignoreAnnotationsKey, true)
    val ignoreUsings = options.getValue(ignoreUsingsKey, true)
    val ignoreIdentifiers = options.getValue(ignoreIdentifiersKey, true)

    cpdLanguage.setProperties(
      languageProperties(
        ignoreLiterals = ignoreLiterals,
        ignoreIdentifiers = ignoreIdentifiers,
        ignoreAnnotations = ignoreAnnotations,
        ignoreUsings = ignoreUsings))

    val cfg = new CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    cfg.setIgnoreAnnotations(ignoreAnnotations)
    cfg.setSkipLexicalErrors(options.getValue(skipLexicalErrorsKey, true))
    cfg.setMinimumTileSize(options.getValue(minimumTileSizeKey, defaultMinToken))
    cfg.setIgnoreIdentifiers(ignoreIdentifiers)
    cfg.setIgnoreLiterals(ignoreLiterals)
    cfg.setIgnoreUsings(ignoreUsings)
    cfg
  }

  private def duplicationClone(m: Match, rootDirectory: Path): DuplicationClone = {
    val files: List[DuplicationCloneFile] = m.getMarkSet.asScala.view.map { mark =>
      val file = rootDirectory.relativize(Paths.get(mark.getFilename))
      DuplicationCloneFile(file.toString, mark.getBeginLine, mark.getEndLine)
    }.to(List)

    DuplicationClone(m.getSourceCodeSlice, m.getTokenCount, m.getLineCount, files)
  }

  private def languageProperties(ignoreLiterals: Boolean,
                                 ignoreIdentifiers: Boolean,
                                 ignoreAnnotations: Boolean,
                                 ignoreUsings: Boolean): Properties = {
    val p = System.getProperties()
    if (ignoreLiterals) {
      p.setProperty(Tokenizer.IGNORE_LITERALS, "true")
    }
    if (ignoreIdentifiers) {
      p.setProperty(Tokenizer.IGNORE_IDENTIFIERS, "true")
    }
    if (ignoreAnnotations) {
      p.setProperty(Tokenizer.IGNORE_ANNOTATIONS, "true")
    }
    if (ignoreUsings) {
      p.setProperty(Tokenizer.IGNORE_USINGS, "true")
    }
    p
  }

}
