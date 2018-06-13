package com.codacy.duplication.pmd

import codacy.docker.api.duplication.{DuplicationClone, DuplicationCloneFile}
import codacy.docker.api.{DuplicationConfiguration, Source}
import com.codacy.api.dtos.{Language, Languages}
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import _root_.scala.collection.GenTraversableOnce
import _root_.scala.util.{Success, Try}

class CpdSpec extends Specification {

  val targetDir = "src/test/resources"

  "Cpd" should {
    "get duplication" in {
      " without defining a language, have 1 clone in 2 Scala files" in noLanguageTest()
      " have 1 clone in 2 Scala files" in scalaTest()
      " have 2 clones in 2 Java files" in javaTest()
      " have 2 clones in 2 Python files" in pythonTest()
      " have clones in 2 Javascript files" in javascriptTestGeneric()
      " not have clones in comments" in javascriptTestComments()
      " have 2 clones in 2 CSharp files" in cSharpTest()
    }
  }

  private def javascriptTestGeneric(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/javascript/misc"
    val clonesTry: Try[List[DuplicationClone]] =
      executeDuplication(codePath, Some(Languages.Javascript))

    clonesTry must beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) => {
        clones should haveLength(1)

        val clone = clones.head
        testClone(clone)(codePath, 194, 428, 2)
      }
    }
  }

  private def javascriptTestComments(): MatchResult[Try[List[DuplicationClone]]] = {
    val commentsPath = "com/codacy/duplication/pmd/javascript/comments"
    val clonesTry: Try[List[DuplicationClone]] =
      executeDuplication(commentsPath, Some(Languages.Javascript))

    clonesTry must beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(1)

        val clone = clones.head
        testClone(clone)(commentsPath, 18, 98, 2)
    }
  }

  private def noLanguageTest(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/scala"
    val clonesTry = executeDuplication(codePath, None)

    clonesTry must beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(1)

        val clone = clones.head
        testClone(clone)(codePath, 18, 131, 2)
    }
  }

  private def scalaTest(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/scala"
    val clonesTry = executeDuplication(codePath, Some(Languages.Scala))

    clonesTry must beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(1)

        val clone = clones.head
        testClone(clone)(codePath, 18, 131, 2)
    }
  }

  private def javaTest(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/java"
    val clonesTry = executeDuplication(codePath, Some(Languages.Java))

    clonesTry should beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(1)

        val clone = clones.head
        testClone(clone)(codePath, 28, 115, 2)

    }
  }

  private def pythonTest(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/python"
    val clonesTry = executeDuplication(codePath, Some(Languages.Python))

    clonesTry should beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(2)
        testClone(clones.head)(codePath, 31, 159, 2)
        testClone(clones.drop(1).head)(codePath, 33, 69, 2)
    }
  }

  private def cSharpTest(): MatchResult[Try[List[DuplicationClone]]] = {
    val codePath = "com/codacy/duplication/pmd/csharp"
    val clonesTry = executeDuplication(codePath, Some(Languages.CSharp))

    clonesTry should beSuccessfulTry

    clonesTry must beLike {
      case Success(clones) =>
        clones must haveLength(2)

        testClone(clones.head)(codePath, 24, 66, 2)
        testClone(clones(1))(codePath, 9, 53, 2)
    }
  }

  private def executeDuplication(dir: String,
                                 language: Option[Language],
                                 options: Map[DuplicationConfiguration.Key, DuplicationConfiguration.Value] = Map.empty)
    : Try[List[DuplicationClone]] = {
    Cpd(path = Source.Directory(s"$targetDir/$dir"), language = language, options)
  }

  private def testClone(clone: DuplicationClone)(
    dir: String,
    nrLines: Int,
    nrTokens: Int,
    filesNr: Int): MatchResult[GenTraversableOnce[DuplicationCloneFile]] = {

    clone.nrLines must beEqualTo(nrLines)
    clone.nrTokens must beEqualTo(nrTokens)
    clone.files must haveLength(filesNr)

    forall(clone.files) { file =>
      (file.filePath must not startWith "/") and
        (file.filePath must not contain s"$targetDir/$dir")
    }
  }

}
