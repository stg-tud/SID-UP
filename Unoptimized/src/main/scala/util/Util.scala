package util

object Util {
  import scala.language.implicitConversions
  implicit def intWithTimes(value: Int) = new {
    def times(op: => Unit): Unit = {
      (1 to value).foreach(_ => op);
    }
  }
}