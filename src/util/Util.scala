package util

object Util {
  implicit def intWithTimes(value: Int) = new {
    def times(op: => Unit) {
      (1 to value).foreach(_ => op);
    }
  }
  def nullSafeEqual(a : Any, b : Any) : Boolean = {
    if(a == b) return true;
    if((a != null && b == null) || (a == null && b != null)) return false;
    return a.equals(b);
  }
}