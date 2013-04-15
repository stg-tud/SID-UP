package reactive
package signals

trait Var[A] extends Signal[A] with ReactiveSource[A, SignalNotification[A]]

object Var {
  def apply[A](value: A): Var[A] = new impl.VarImpl(value)
}