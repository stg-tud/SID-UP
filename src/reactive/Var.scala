package reactive
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.SynchronizedMap

class Var[A](name: String, initialValue: A, initialEvent : Event) extends SignalImpl[A](name, initialValue) with ReactiveSource[A] {
  def set(value: A) = {
    emit(value);
  }

  override protected[reactive] def emit(event: Event, newValue: A) {
    updateValue(event, newValue);
  }
  override protected[reactive] def emit(event: Event) {
    emit(event, value);
  }
}

object Var {
  def apply[A](name: String, value: A): Var[A] = new Var(name, value, new Event(Map()))
  def apply[A](value: A): Var[A] = apply("AnonVar", value)
}