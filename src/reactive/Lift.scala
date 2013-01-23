package reactive

object Lift {
  implicit def liftEventStream[A, B](fun : A=>B) = (a : EventStream[A]) => a.map(fun) 
  
  implicit def lift1[A, B](fun : A => B) = (a : Signal[A]) => Signal(makeName(fun, a), a) { fun(a) }
  
  implicit def lift2[A1, A2, B](fun: (A1, A2) => B) = (a1 : Signal[A1], a2 : Signal[A2]) => Signal(makeName(fun, a1, a2), a1, a2) { fun(a1, a2) }
  implicit def lift2a[A1, A2, B](fun: (A1, A2) => B) = (a1 : Signal[A1], a2 : A2) => Signal(makeName(fun, a1, a2), a1) { fun(a1, a2) }
  implicit def lift2b[A1, A2, B](fun: (A1, A2) => B) = (a1 : A1, a2 : Signal[A2]) => Signal(makeName(fun, a1, a2), a2) { fun(a1, a2) }
  
  implicit def lift3[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: Signal[A1], a2: Signal[A2], a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a1, a2, a3) { fun(a1, a2, a3) }
  implicit def lift3a[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: Signal[A1], a2: A2, a3: A3) => Signal(makeName(fun, a1, a2, a3), a1) { fun(a1, a2, a3) }
  implicit def lift3b[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: A1, a2: Signal[A2], a3: A3) => Signal(makeName(fun, a1, a2, a3), a2) { fun(a1, a2, a3) }
  implicit def lift3c[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: A1, a2: A2, a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a3) { fun(a1, a2, a3) }
  implicit def lift3ab[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: Signal[A1], a2: Signal[A2], a3: A3) => Signal(makeName(fun, a1, a2, a3), a1, a2) { fun(a1, a2, a3) }
  implicit def lift3bc[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: Signal[A1], a2: A2, a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a1, a3) { fun(a1, a2, a3) }
  implicit def lift3ac[A1, A2, A3, B](fun: (A1, A2, A3) => B)= (a1: A1, a2: Signal[A2], a3: Signal[A3]) => Signal(makeName(fun, a1, a2, a3), a2, a3) { fun(a1, a2, a3) }
  
  private def makeName(fun : Any, params : Any*) : String = "lifted["+fun.toString()+"]("+params.map(param => if(param.isInstanceOf[Signal[_]]) param.asInstanceOf[Signal[_]].name else String.valueOf(param)).mkString(", ")+")"
}