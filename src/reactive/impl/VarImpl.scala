package reactive.impl
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.SynchronizedMap
import reactive.Var
import reactive.Event
import reactive.PropagationData

class VarImpl[A](name: String, initialValue: A, initialEvent: Event) extends SignalImpl[A](name, initialValue) with Var[A] {
  def set(value: A) = {
    emit(value);
  }

  override protected[reactive] def emit(event: Event, maybeNewValue: Option[A]) {
    updateValue(new PropagationData(event, Nil, Nil)) { maybeNewValue.getOrElse(_)}
  }
}