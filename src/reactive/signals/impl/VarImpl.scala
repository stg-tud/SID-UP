package reactive
package signals
package impl

import reactive.impl.ReactiveSourceImpl
import util.Util

class VarImpl[A](initialValue: A) extends SignalImpl[A](null, initialValue) with ReactiveSourceImpl[A, SignalNotification[A]] with Var[A] {
  override def emit(transaction : Transaction, newValue: A) {
    publish(new SignalNotification(transaction, noDependencyChange, value.update(newValue)));
  }
}