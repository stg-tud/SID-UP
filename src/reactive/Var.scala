package reactive
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.SynchronizedMap
import reactive.impl.VarImpl

trait Var[A] extends Signal[A] with ReactiveSource[A] {
  def set(value: A)
}

object Var {
  def apply[A](name: String, value: A): Var[A] = new VarImpl(name, value)
  def apply[A](value: A): Var[A] = apply("AnonVar", value)
}