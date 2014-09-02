package reactive

import reactive.signals.Val
import reactive.signals.impl.FunctionalSignal
import reactive.events.EventStream
import reactive.signals.Signal

object Lift {
  import scala.language.implicitConversions
  implicit def valueToSignal[A](value: A): Signal[A] = new Val(value)

  implicit def signal1[A1, B](fun: A1 => B): Signal[A1] => Signal[B] = a => a.map(fun)
  implicit def signal2[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], Signal[A2]) => Signal[B] = (a1, a2) => new FunctionalSignal({ t => fun(a1.value(t), a2.value(t)) }, a1, a2)
  implicit def signal3[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], Signal[A3]) => Signal[B] = (a1, a2, a3) => new FunctionalSignal({ t => fun(a1.value(t), a2.value(t), a3.value(t)) }, a1, a2, a3)

  implicit def signalSink1[A](fun: A => Unit): Signal[A] => Unit = a => { fun(a.now); a.observe(fun) }
  implicit def signalSink2[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], Signal[A2]) => Unit = (a1, a2) => signalSink1(fun.tupled)(new FunctionalSignal({ t => (a1.value(t), a2.value(t)) }, a1, a2))
  implicit def signalSink3[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], Signal[A3]) => Unit = (a1, a2, a3) => signalSink1(fun.tupled)(new FunctionalSignal({ implicit t => (a1.value(t), a2.value(t), a3.value(t)) }, a1, a2, a3))

  implicit def eventStream1[A, B](fun: A => B): EventStream[A] => EventStream[B] = a => a.map(fun)
  implicit def eventStreamSink1[A](fun: A => Unit): EventStream[A] => Unit = a => a.observe(fun)

  implicit def eventStream2a[A1, A2, B](fun: (A1, A2) => B): (EventStream[A1], Signal[A2]) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStream2b[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], EventStream[A2]) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStream3a[A1, A2, A3, B](fun: (A1, A2, A3) => B): (EventStream[A1], Signal[A2], Signal[A3]) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStream3b[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], EventStream[A2], Signal[A3]) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStream3c[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], EventStream[A3]) => EventStream[B] = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3]) => throw new UnsupportedOperationException("not implemented yet")

  implicit def eventStreamSink2a[A1, A2](fun: (A1, A2) => Unit): (EventStream[A1], Signal[A2]) => Unit = (a1: EventStream[A1], a2: Signal[A2]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStreamSink2b[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], EventStream[A2]) => Unit = (a1: Signal[A1], a2: EventStream[A2]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStreamSink3a[A1, A2, A3](fun: (A1, A2, A3) => Unit): (EventStream[A1], Signal[A2], Signal[A3]) => Unit = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStreamSink3b[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], EventStream[A2], Signal[A3]) => Unit = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3]) => throw new UnsupportedOperationException("not implemented yet")
  implicit def eventStreamSink3c[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], EventStream[A3]) => Unit = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3]) => throw new UnsupportedOperationException("not implemented yet")
}
