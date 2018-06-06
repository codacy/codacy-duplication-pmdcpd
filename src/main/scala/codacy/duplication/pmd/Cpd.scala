package codacy.duplication.pmd

import java.io.{ByteArrayOutputStream, OutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths}

import better.files.File
import codacy.docker.api.{
  DuplicationConfiguration,
  DuplicationConfigurationValue,
  Source
}
import com.codacy.api.dtos.{Language, Languages}
import net.sourceforge.pmd.cpd._
import play.api.libs.json.{JsNumber, JsValue}
import codacy.docker.api.duplication._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object Cpd extends DuplicationTool {
  override def apply(path: Source.Directory,
                     language: Option[Language],
                     options: Map[DuplicationConfiguration.Key,
                                  DuplicationConfiguration.Value])
    : Try[List[DuplicationClone]] = {
    val baos = new ByteArrayOutputStream()
    val stdErr = System.err

    Try {
      System.setErr(new PrintStream(baos, true, "utf-8"))
      val directoryPath = (File.currentWorkingDirectory / path.path).path
      val configuration =
        getConfiguration(language, options).getOrElse(new CPDConfiguration())
      val cpd = new CPD(configuration)
      cpd.addRecursively(directoryPath.toFile)

      System.setErr(stdErr)

      cpd.go()

      val res = cpd.getMatches
      res.asScala.map(matchToClone(_, directoryPath))
    } match {
      case Success(matches) => Success(matches.toList)
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

  private def getConfiguration(languageOpt: Option[Language],
                               options: Map[DuplicationConfiguration.Key,
                                            DuplicationConfiguration.Value]) = {

    val ignoreAnnotations = true
    val skipLexicalErrors = true

    val configOpt: Option[(net.sourceforge.pmd.cpd.Language, Int)] =
      languageOpt.flatMap {
        case Languages.Python     => Some((new PythonLanguage, 50))
        case Languages.Ruby       => Some((new RubyLanguage, 50))
        case Languages.Java       => Some((new JavaLanguage, 100))
        case Languages.Javascript => Some((new EcmascriptLanguage, 40))
        case Languages.Scala =>
          val cpdScala = new ScalaLanguage
          object ScalaL
              extends AbstractLanguage(cpdScala.getName,
                                       cpdScala.getTerseName,
                                       ScalaTokenizer,
                                       cpdScala.getExtensions.asScala: _*)
          Some((ScalaL, 50))
        case Languages.CSharp => Some((new CsLanguage, 50))
        case _                => None
      }

    configOpt.map {
      case (cpdLanguage, defaultMinToken) => {
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

        val cfg = new CPDConfiguration()
        cfg.setLanguage(cpdLanguage)
        cfg.setIgnoreAnnotations(ignoreAnnotations)
        cfg.setSkipLexicalErrors(skipLexicalErrors)
        cfg.setMinimumTileSize(minTokenMatch)
        cfg
      }
    }
  }

  private def matchToClone(m: Match, rootDirectory: Path): DuplicationClone = {
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
