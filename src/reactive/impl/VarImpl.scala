package reactive
package impl

import Reactive._

class VarImpl[A](name: String, initialValue: A) extends SignalImpl[A](name, initialValue) with ReactiveSourceImpl[A] with Var[A] {
  def set(value: A) {
    emit(value);
  }
}