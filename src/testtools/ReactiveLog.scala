package testtools
import reactive.Reactive
import scala.collection.mutable
import reactive.Signal

class ReactiveLog[A](reactive: Reactive[A]) {
  private val _values = mutable.MutableList[A]()
  if(reactive.isInstanceOf[Signal[_]]) _values += reactive.asInstanceOf[Signal[A]].value
  reactive.observe { value => _values += value }

  def values = _values.toList
  def assert(expected: A*) {
    ReactiveLog.assert(expected.toList, values);
  }
}

object ReactiveLog {
  class AssertionFailure(msg : String) extends RuntimeException(msg);

  def assert(expected: Any, actual : Any) {
    val msg = "expected: " + expected + ", actual: " + actual
    if (expected.equals(actual)) {
      println("[OK] "+msg); ;
    } else {
      throw new ReactiveLog.AssertionFailure("[Assertion Violation] "+msg);
    }
  }
}
