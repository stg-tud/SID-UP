package reactive

object Lift {
  def apply[A, B](fun: A => B)(a: Reactive[A]) = Signal(makeName(fun, a), a) { fun(a) }
  def apply[A1, A2, B](fun: (A1, A2) => B)(a1: Reactive[A1], a2: Reactive[A2]) = Signal(makeName(fun, a1, a2), a1, a2) { fun(a1, a2) }
  def apply[A1, A2, A3, B](fun: (A1, A2, A3) => B)(a1: Reactive[A1], a2: Reactive[A2], a3: Reactive[A3]) = Signal(makeName(fun, a1, a2, a3), a1, a2, a3) { fun(a1, a2, a3) }
  def apply[A1, A2, A3, A4, B](fun: (A1, A2, A3, A4) => B)(a1: Reactive[A1], a2: Reactive[A2], a3: Reactive[A3], a4: Reactive[A4]) = Signal(makeName(fun, a1, a2, a3, a4), a1, a2, a3, a4) { fun(a1, a2, a3, a4) }
  def apply[A1, A2, A3, A4, A5, B](fun: (A1, A2, A3, A4, A5) => B)(a1: Reactive[A1], a2: Reactive[A2], a3: Reactive[A3], a4: Reactive[A4], a5: Reactive[A5]) = Signal(makeName(fun, a1, a2, a3, a4, a5), a1, a2, a3, a4, a5) { fun(a1, a2, a3, a4, a5) }
  private def makeName(fun : Any, params : Reactive[_]*) = "lifted["+fun.toString()+"]("+params.map(_.name).mkString(", ")+")"
}