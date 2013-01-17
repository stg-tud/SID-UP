package reactive

object Lift {
  def apply[A, B](fun: A => B)(a: Signal[A]) = Signal(makeName(fun, a), a) { fun(a) }
  def apply[A1, A2, B](fun: (A1, A2) => B)(a1: Signal[A1], a2: Signal[A2]) = Signal(makeName(fun, a1, a2), a1, a2) { fun(a1, a2) }
  def apply[A1, A2, A3, B](fun: (A1, A2, A3) => B)(a1: Signal[A1], a2: Signal[A2], a3: Signal[A3]) = Signal(makeName(fun, a1, a2, a3), a1, a2, a3) { fun(a1, a2, a3) }
  def apply[A1, A2, A3, A4, B](fun: (A1, A2, A3, A4) => B)(a1: Signal[A1], a2: Signal[A2], a3: Signal[A3], a4: Signal[A4]) = Signal(makeName(fun, a1, a2, a3, a4), a1, a2, a3, a4) { fun(a1, a2, a3, a4) }
  def apply[A1, A2, A3, A4, A5, B](fun: (A1, A2, A3, A4, A5) => B)(a1: Signal[A1], a2: Signal[A2], a3: Signal[A3], a4: Signal[A4], a5: Signal[A5]) = Signal(makeName(fun, a1, a2, a3, a4, a5), a1, a2, a3, a4, a5) { fun(a1, a2, a3, a4, a5) }
  private def makeName(fun : Any, params : Signal[_]*) = "lifted["+fun.toString()+"]("+params.map(_.name).mkString(", ")+")"
}