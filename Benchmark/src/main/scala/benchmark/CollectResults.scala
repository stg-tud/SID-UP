package benchmark

import javax.swing.JFileChooser
import java.io.File
import scala.collection.immutable.SortedMap
import java.util.regex.PatternSyntaxException

object CollectResults extends App {
  def mapTransformSingleValue[K, V](map: Map[K, V], key: K)(transformation: V => V) = {
    val old = map(key)
    val updated = transformation(old)
    map + (key -> updated)
  }
  val chooser = new JFileChooser(System.getProperty("user.dir"))
  chooser.setDialogTitle("Directory to scan?")
  chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
  chooser.setMultiSelectionEnabled(false)
  if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
    val choice = chooser.getSelectedFile()
    def readFile(file: File, results: Map[String, Map[String, String]]): Map[String, Map[String, String]] = {
      val fileNameParts = file.getName().split('.')
      val experiment = fileNameParts(0).replace('_', ' ')
      val measurement = file.getAbsolutePath().substring(choice.getAbsolutePath().length() + 1).split(File.separatorChar).toList.dropRight(1).mkString(" ") + " " + fileNameParts(1)

      val lines = scala.io.Source.fromFile(file).getLines
      lines.next
      val theLine = lines.next
      val theValue = theLine.split('\t').last

      mapTransformSingleValue(results, experiment) { _ + (measurement -> theValue) }
    }
    def scanDirectory(dir: File, results: Map[String, Map[String, String]]): Map[String, Map[String, String]] = {
      dir.listFiles().foldLeft(results) { (results, file) =>
        if (file.isFile() && file.getName().endsWith(".tsv")) {
          readFile(file, results)
        } else if (file.isDirectory()) {
          scanDirectory(file, results)
        } else {
          results
        }
      }
    }
    val table = scanDirectory(choice, Map().withDefaultValue(SortedMap()))
    println("\t"+table.values.head.keys.mkString("\t"))
    table.foreach {
      case (experiment, measurements) =>
        println(experiment+"\t"+measurements.values.mkString("\t"))
    }
  }
}