package testtools
import reactive.Reactive
import scala.collection.mutable
import reactive.Signal
import util.Util

class ReactiveLog[A](reactive: Reactive[A]) {
  private val _values = mutable.MutableList[A]()
  if(reactive.isInstanceOf[Signal[_]]) _values += reactive.asInstanceOf[Signal[A]].now
  reactive.observe { value => _values += value }

  def values = _values.toList
  def assert(expected: A*) {
    ReactiveLog.assert(expected.toList, values);
  }
}

object ReactiveLog {
  class AssertionFailure(msg : String) extends RuntimeException("[Assertion Violation] "+msg);

  def assert(expected: Any, actual : Any) {
    val msg = "expected: " + expected + ", actual: " + actual
    if (Util.nullSafeEqual(expected, actual)) {
      println("[OK] "+msg); ;
    } else {
      throw new ReactiveLog.AssertionFailure(msg);
    }
  }
  def assert(condition : Boolean) {
    assert(true, condition);
  }
}
