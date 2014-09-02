package benchmark

import org.scalameter._
import org.scalameter.utils.Tree
import scalax.file.Path

class GnuplotReporter extends Reporter {

  val delimiter = "\t"
  val newline = "\n"
  val newblock = "\n\n\n"
  val resultdir = initialContext.goe(Key.reports.resultDir, "tmp")

  def report(cd: CurveData, persistor: Persistor): Unit = {
    val file = Path(resultdir, s"${cd.context.scope}.tsv")
    file.deleteIfExists()
    file.write(format(cd, persistor))
  }

  def report(result: Tree[CurveData], persistor: Persistor) = {
    result.foreach { report(_, persistor) }
    true
  }

  def formatHeader(cd: CurveData) = (cd.measurements.head.params.axisData.keys.toSeq.map { key => s"param-$key" } :+ "value").mkString(delimiter)

  def formatBody(cd: CurveData) = {
    val measurements = cd.measurements.map { measurement =>
      measurement.params.axisData.values.toSeq :+ measurement.value
    }
    def groupFormatMeasurements(xs: Seq[Seq[Any]]): Seq[String] =
      if (xs.isEmpty) Seq()
      else {
        val (firstGroup, rest) = xs.span(_(1) == xs(0)(1))
        Seq(firstGroup.map { _.mkString(delimiter) }.mkString(newline)) ++
          groupFormatMeasurements(rest)
      }
    groupFormatMeasurements(measurements).map(formatHeader(cd) + newline + _).mkString(newblock)
  }

  def format(cd: CurveData, persistor: Persistor) = {
    val head = formatHeader(cd)
    val history = persistor.load(cd.context)
    val body = history.curves.map { formatBody }.mkString(newblock)
    body
  }

}
