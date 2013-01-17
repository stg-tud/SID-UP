package test
import reactive.Reactive
import scala.collection.mutable
import reactive.Signal

class ReactiveLog[A](reactive: Reactive[A]) {
  private val _values = mutable.MutableList[A]()
  if(reactive.isInstanceOf[Signal[_]]) _values += reactive.asInstanceOf[Signal[A]].value
  reactive.observe { value => _values += value }

  def values = _values.toList
  def assert(expected: A*) {
    val msg = "expected: " + expected.toList + ", actual: " + values
    if (expected.toList.equals(values)) {
      println("[OK] "+msg); ;
    } else {
      throw new RuntimeException("[Assertion Violation] "+msg);
    }
  }
}
