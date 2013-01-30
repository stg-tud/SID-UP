package testtools
import reactive.Reactive
import scala.collection.mutable
import reactive.Signal
import util.Util

object Asserts {
  class AssertionFailure(msg : String) extends RuntimeException("[Assertion Violation] "+msg);

  def assert(expected: Any, actual : Any) {
    val msg = "expected: " + expected + ", actual: " + actual
    if (Util.nullSafeEqual(expected, actual)) {
      println("[OK] "+msg); ;
    } else {
      throw new Asserts.AssertionFailure(msg);
    }
  }
  def assert(condition : Boolean) {
    assert(true, condition);
  }
}
