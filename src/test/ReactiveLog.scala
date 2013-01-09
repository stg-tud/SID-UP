package test
import reactive.Reactive
import scala.collection.mutable

class ReactiveLog[A](reactive: Reactive[A]) {
  private val _values = mutable.MutableList(reactive.value)
  reactive.observe { value => _values += value }

  def values = _values.toList
  def assert(expected: List[A]) {
    val msg = "Expected: " + expected + ", actual: " + values
    if (expected.equals(values)) {
      println(msg); ;
    } else {
      throw new RuntimeException(msg);
    }
  }
}