package com.codacy.duplication.pmd

import _root_.java.io.{ByteArrayOutputStream, PrintStream}
import _root_.java.nio.charset.StandardCharsets
import _root_.java.nio.file.{Path, Paths}

import better.files._
import com.codacy.docker.api.duplication._
import com.codacy.plugins.api.duplication.{DuplicationClone, DuplicationCloneFile, DuplicationTool}
import com.codacy.plugins.api.languages.{Language, Languages}
import com.codacy.plugins.api.{Options, Source}
import net.sourceforge.pmd.cpd.{Language => CPDLanguage, _}

import _root_.scala.collection.JavaConverters._
import _root_.scala.util.{Failure, Success, Try}

object Cpd extends DuplicationTool {

  // This should ideally come from configuration so changing the config is automatically propagated here.
  // For now we are hardcoding it here which is defined here:
  // https://github.com/codacy/codacy-worker/blob/6f2bee63b1c42a19f05b7b73497da001740d9e14/conf/application.conf#L107
  private val MaxFileSize = 150000

  private val allLanguages: List[Language] =
    List[Language](
      Languages.CSharp,
      Languages.C,
      Languages.CPP,
      Languages.Javascript,
      Languages.Go,
      Languages.Java,
      Languages.PLSQL,
      Languages.Python,
      Languages.Ruby,
      Languages.Scala,
      Languages.Swift)

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
          .filter(
            file =>
              file.isRegularFile &&
                language.extensions.contains(file.extension.getOrElse(file.name)) &&
                file.size < MaxFileSize)
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
      case Languages.PLSQL             => cpdConfiguration(new PLSQLLanguage, 100, options)
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
    // We want to use PMDCPD defaults. However, this is a breaking change and needs to be considered.
    // Remove the `.orElse(Some(true))` to use the PMDCPD defaults.
    val ignoreLiterals = options.getValue[Boolean]("ignoreLiterals").orElse(Some(true))
    val ignoreAnnotations = options.getValue[Boolean]("ignoreAnnotations").orElse(Some(true))
    val ignoreUsings = options.getValue[Boolean]("ignoreUsings").orElse(Some(true))
    val ignoreIdentifiers = options.getValue[Boolean]("ignoreIdentifiers").orElse(Some(false))
    val skipLexicalErrors = options.getValue[Boolean]("skipLexicalErrors").orElse(Some(true))
    // This is mandatory in PMDCPD
    val minimumTileSize = options.getValue[Int]("minTokenMatch").getOrElse(defaultMinToken)

    val cfg = new CPDConfiguration()
    cfg.setLanguage(cpdLanguage)
    ignoreAnnotations.foreach(cfg.setIgnoreAnnotations)
    skipLexicalErrors.foreach(cfg.setSkipLexicalErrors)
    ignoreIdentifiers.foreach(cfg.setIgnoreIdentifiers)
    ignoreLiterals.foreach(cfg.setIgnoreLiterals)
    ignoreUsings.foreach(cfg.setIgnoreUsings)
    cfg.setMinimumTileSize(minimumTileSize)
    CPDConfiguration.setSystemProperties(cfg)
    cfg
  }

  private def duplicationClone(m: Match, rootDirectory: Path): DuplicationClone = {
    val files: List[DuplicationCloneFile] = m.getMarkSet.asScala.view.map { mark =>
      val file = rootDirectory.relativize(Paths.get(mark.getFilename))
      DuplicationCloneFile(file.toString, mark.getBeginLine, mark.getEndLine)
    }.to(List)

    DuplicationClone(m.getSourceCodeSlice, m.getTokenCount, m.getLineCount, files)
  }

}
