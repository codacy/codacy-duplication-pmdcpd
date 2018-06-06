package codacy.duplication.pmd

import codacy.docker.api.Source
import codacy.docker.api.duplication.{DuplicationClone, DuplicationCloneFile}
import com.codacy.api.dtos.Languages
import org.specs2.mutable.Specification

import scala.util.Try

class CpdSpec extends Specification {

  val targetDir = "src/test/resources"

  "Cpd" should {
    "get duplication" in {
      "for scala" in {

        val expectedDuplicationResults = List(
          DuplicationClone(
            s"""  def newtonsMethod(fx: Double => Double,
                    fxPrime: Double => Double,
                    x: Double,
                    tolerance: Double): Double = {
    var x1 = x; var xNext = x1 - fx(x1) / fxPrime(x1); xNext
  }""",
            54,
            7,
            List(DuplicationCloneFile("codacy/duplication/pmd/example.scala",
                                      36,
                                      42),
                 DuplicationCloneFile("codacy/duplication/pmd/example.scala",
                                      43,
                                      49))
          ))

        val duplicationResults: Try[List[DuplicationClone]] =
          Cpd(path = Source.Directory(targetDir),
              language = Some(Languages.Scala),
              Map.empty)

        duplicationResults should beSuccessfulTry(expectedDuplicationResults)
      }

      "for java" in {
        val expectedDuplicationResults = List(
          DuplicationClone(
            """    public static void doStuff(){
            |        String x = "hello cruel world"
            |        for(int i = 0; i < x.length(); i++){
            |            System.out.println(x.charAt(i))
            |        }
            |        System.out.println("End of hello");
            |        System.out.println(":P");
            |        String x = "hello cruel world"
            |        for(int i = 0; i < x.length(); i++){
            |            System.out.println(x.charAt(i))
            |        }
            |        System.out.println("End of hello");
            |        System.out.println(":P");
            |
            |    }""".stripMargin,
            106,
            15,
            List(DuplicationCloneFile("codacy/duplication/pmd/example.java",
                                      9,
                                      23),
                 DuplicationCloneFile("codacy/duplication/pmd/example.java",
                                      25,
                                      39))
          ))

        val duplicationResults: Try[List[DuplicationClone]] =
          Cpd(path = Source.Directory(targetDir),
              language = Some(Languages.Java),
              Map.empty)

        duplicationResults should beSuccessfulTry(expectedDuplicationResults)
      }

      "for python" in {
        val expectedDuplicationResults = List(
          DuplicationClone(
            s"""def my_function_with_args(username, greeting):
           |    print("Yoooooooo")
           |    y = 1 + 1
           |    y += y
           |    y += y
           |    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))
           |    print("Yoooooooo")
           |    y = 1 + 1
           |    y += y
           |    y += y
           |    print("Hello, %s , From My Function!, I wish you %s"%(username, greeting))
           |
           |def my_function_with_args1(username, greeting):""".stripMargin,
            57,
            13,
            List(
              DuplicationCloneFile("codacy/duplication/pmd/example.py", 5, 17),
              DuplicationCloneFile("codacy/duplication/pmd/example.py", 17, 29))
          ))

        val duplicationResults: Try[List[DuplicationClone]] =
          Cpd(path = Source.Directory(targetDir),
              language = Some(Languages.Python),
              Map.empty)

        duplicationResults should beSuccessfulTry(expectedDuplicationResults)
      }

      "without language setting" in {
        //TODO
      }
    }
  }

}
