package com.codacy.duplication.pmd.scala

import java.io
import java.io.File

import codacy.foundation.api.{Directory, FileContents}
import codacy.foundation.files.FileSystemProvider
import codacy.foundation.files.FileSystemProvider1
import codacy.foundation.files.FileSystemProvider2
import codacy.foundation.files.FileSystemProvider3
import model.project.metrics.Clone

/* a block comment */

// and a line comment

trait test2 {

  protected def runTool(rootCanonicalPath: String, outputFile: File): Option[Seq[String]]

  protected def parseClones(rootCanonicalPath: String, lines: Seq[String]): Option[Seq[Clone]]

  def getClones(directory: Directory): Option[Seq[Clone]] = {
    FileSystemProvider.withRandomFile { outputFile =>
      val rootCanonicalPath = new io.File(directory.path).getCanonicalPath
      runTool(rootCanonicalPath, outputFile).flatMap { output =>
        FileContents.getLines(outputFile).flatMap { lines =>
          parseClones(rootCanonicalPath, lines)
        }
      }
    }
  }

}
