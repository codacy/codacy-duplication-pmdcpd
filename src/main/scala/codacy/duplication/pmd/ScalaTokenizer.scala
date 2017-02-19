package codacy.duplication.pmd

import java.io.File

import net.sourceforge.pmd.cpd.{Tokens => CpdTokens, _}
import net.sourceforge.pmd.lang.ast.TokenMgrError

import scala.util.{Failure, Success, Try}

private[pmd] object ScalaTokenizer extends Tokenizer {

  import scala.meta.{Tree => MetaTree, _}

  override def tokenize(sourceCode: SourceCode, tokenEntries: CpdTokens): Unit = {

    val fileName = sourceCode.getFileName

    Try(new File(fileName).parse[Source]) match {
      case Success(Parsed.Success(tree)) =>
        matchesInTree(tree, fileName).foreach(tokenEntries.add)
        tokenEntries.add(TokenEntry.getEOF)
      case Success(Parsed.Error(position, message, details)) => throw new TokenMgrError(
        s"Lexical error in file $fileName. The scala tokenizer exited with error on $position: " + message + details,
        TokenMgrError.LEXICAL_ERROR
      )
      case Failure(error) => throw new TokenMgrError(
        s"Lexical error in file $fileName. The scala tokenizer exited with error: " + error.getMessage,
        TokenMgrError.LEXICAL_ERROR
      )
    }
  }

  private[this] def matchesInTree(tree: MetaTree, filename: String) = {
    import Token._
    val tokens = Option(tree).toSeq.flatMap {
      case t@source"..${stats: Seq[Stat]}" if stats.size == 1 =>
        stats.headOption.collect { case q"package $ref { ..${stats: Seq[Stat]} }" =>
          stats.dropWhile {
            case q"import ..$importersnel" => true
            case _ => false
          }
        }.getOrElse(Seq(t))
      case nonDefaultTree => Seq(nonDefaultTree)
    }.flatMap(_.tokens.filterNot(t => t.isInstanceOf[Comment] || t.isInstanceOf[Space]))

    tokens.collect {
      case token if !token.isInstanceOf[EOF] =>
        val str = token match {
          case _: BOF => "BOF"
          case other => other.show[Syntax]
        }
        new TokenEntry(str, filename, token.pos.start.line + 1)
    }
  }

}
