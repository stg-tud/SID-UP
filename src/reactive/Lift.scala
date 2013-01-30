package reactive

object Lift {
  implicit def lift1EventStream[A, B](fun: A => B) = (a: EventStream[A]) => a.map(fun)
  implicit def lift1Signal[A, B](fun: A => B) = (a: Signal[A]) => Signal(makeName(fun, a), a) { fun(a) }
  implicit def lift1SinkSignal[A](fun: A => Unit) = (a: Signal[A]) => { fun(a.now); a.observe(fun) }
  implicit def lift1SinkEventStream[A](fun: A => Unit) = (a: EventStream[A]) => a.observe(fun)

  implicit def lift2[A1, A2, B](fun: (A1, A2) => B) = (a1: Signal[A1], a2: Signal[A2]) => Signal(makeName(fun, a1, a2), a1, a2) { fun(a1, a2) }
  implicit def lift2a[A1, A2, B](fun: (A1, A2) => B) = (a1: Signal[A1], a2: A2) => Signal(makeName(fun, a1, a2), a1) { fun(a1, a2) }
  implicit def lift2b[A1, A2, B](fun: (A1, A2) => B) = (a1: A1, a2: Signal[A2]) => Signal(makeName(fun, a1, a2), a2) { fun(a1, a2) }
  implicit def lift2Sink[A1, A2](fun: (A1, A2) => Unit) = (a1: Signal[A1], a2 : Signal[A2]) => lift1SinkSignal(fun.tupled)(Signal(a1, a2){(a1, a2)})
  implicit def lift2aSinkSignal[A1, A2](fun: (A1, A2) => Unit) = (a1: Signal[A1], a2 : A2) => lift1SinkSignal({a1 : A1 => fun(a1, a2)})(a1);
  implicit def lift2bSinkSignal[A1, A2](fun: (A1, A2) => Unit) = (a1: A1, a2 : Signal[A2]) => lift1SinkSignal({a2 : A2 => fun(a1, a2)})(a2);
  implicit def lift2aSinkEventStream[A1, A2](fun: (A1, A2) => Unit) = (a1: EventStream[A1], a2 : A2) => lift1SinkEventStream({a1 : A1 => fun(a1, a2)})(a1);
  implicit def lift2bSinkEventStream[A1, A2](fun: (A1, A2) => Unit) = (a1: A1, a2 : EventStream[A2]) => lift1SinkEventStream({a2 : A2 => fun(a1, a2)})(a2);

  implicit def lift3[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: Signal[A1], a2: Signal[A2], a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a1, a2, a3) { fun(a1, a2, a3) }
  implicit def lift3a[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: Signal[A1], a2: A2, a3: A3) => Signal(makeName(fun, a1, a2, a3), a1) { fun(a1, a2, a3) }
  implicit def lift3b[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: A1, a2: Signal[A2], a3: A3) => Signal(makeName(fun, a1, a2, a3), a2) { fun(a1, a2, a3) }
  implicit def lift3c[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: A1, a2: A2, a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a3) { fun(a1, a2, a3) }
  implicit def lift3ab[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: Signal[A1], a2: Signal[A2], a3: A3) => Signal(makeName(fun, a1, a2, a3), a1, a2) { fun(a1, a2, a3) }
  implicit def lift3bc[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: Signal[A1], a2: A2, a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a1, a3) { fun(a1, a2, a3) }
  implicit def lift3ac[A1, A2, A3, B](fun: (A1, A2, A3) => B) = (a1: A1, a2: Signal[A2], a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a2, a3) { fun(a1, a2, a3) }

  private def makeName(fun: Any, params: Any*): String = "lifted[" + fun.toString() + "](" + params.map(param => if (param.isInstanceOf[Signal[_]]) param.asInstanceOf[Signal[_]].name else String.valueOf(param)).mkString(", ") + ")"
}