package test
import reactive.Reactive
import scala.collection.mutable

class ReactiveLog[A](reactive: Reactive[A]) {
  private val _values = mutable.MutableList(reactive.value)
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