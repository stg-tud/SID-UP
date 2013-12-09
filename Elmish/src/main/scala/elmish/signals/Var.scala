package elmish
package signals

trait Var[A] extends Signal[A] with ReactiveSource[A]

object Var {
  def apply[A](value: A): Var[A] = new impl.VarImpl(value)
}