package util

object Util {
  implicit def intWithTimes(value: Int) = new {
    def times(op: => Unit) {
      (1 to value).foreach(_ => op);
    }
  }
}