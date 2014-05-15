package reactive

import reactive.signals.Val
import reactive.signals.impl.FunctionalSignal
import reactive.events.EventStream
import reactive.signals.Signal
import scala.concurrent.stm._

object Lift {
  object single {
    implicit def valueToSignal[A](value: A): Signal[A] = new Val(value)

    implicit def signal1[A1, B](fun: A1 => B): Signal[A1] => Signal[B] = a => a.single.map(fun)
    implicit def signal2[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], Signal[A2]) => Signal[B] = (a1, a2) => atomic { transactional.signal2(fun)(a1, a2, _) }
    implicit def signal3[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], Signal[A3]) => Signal[B] = (a1, a2, a3) => atomic { transactional.signal3(fun)(a1, a2, a3, _) }

    implicit def signalSink1[A](fun: A => Unit): Signal[A] => Unit = a => atomic { transactional.signalSink1(fun)(a, _) }
    implicit def signalSink2[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], Signal[A2]) => Unit = (a1, a2) => atomic { transactional.signalSink2(fun)(a1, a2, _) }
    implicit def signalSink3[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], Signal[A3]) => Unit = (a1, a2, a3) => atomic { transactional.signalSink3(fun)(a1, a2, a3, _) }

    implicit def eventStream1[A, B](fun: A => B): EventStream[A] => EventStream[B] = a => a.single.map(fun)
    implicit def eventStreamSink1[A](fun: A => Unit): EventStream[A] => Unit = a => a.single.observe(fun)

    implicit def eventStream2a[A1, A2, B](fun: (A1, A2) => B): (EventStream[A1], Signal[A2]) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2]) => ???
    implicit def eventStream2b[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], EventStream[A2]) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2]) => ???
    implicit def eventStream3a[A1, A2, A3, B](fun: (A1, A2, A3) => B): (EventStream[A1], Signal[A2], Signal[A3]) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => ???
    implicit def eventStream3b[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], EventStream[A2], Signal[A3]) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3]) => ???
    implicit def eventStream3c[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], EventStream[A3]) => EventStream[B] = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3]) => ???

    implicit def eventStreamSink2a[A1, A2](fun: (A1, A2) => Unit): (EventStream[A1], Signal[A2]) => Unit = (a1: EventStream[A1], a2: Signal[A2]) => ???
    implicit def eventStreamSink2b[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], EventStream[A2]) => Unit = (a1: Signal[A1], a2: EventStream[A2]) => ???
    implicit def eventStreamSink3a[A1, A2, A3](fun: (A1, A2, A3) => Unit): (EventStream[A1], Signal[A2], Signal[A3]) => Unit = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => ???
    implicit def eventStreamSink3b[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], EventStream[A2], Signal[A3]) => Unit = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3]) => ???
    implicit def eventStreamSink3c[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], EventStream[A3]) => Unit = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3]) => ???
  }
  object transactional {
    implicit def valueToSignal[A](value: A): Signal[A] = new Val(value)

    implicit def signal1[A1, B](fun: A1 => B): (Signal[A1], InTxn) => Signal[B] = (a, inTxn) => a.map(fun)(inTxn)
    implicit def signal2[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], Signal[A2], InTxn) => Signal[B] = (a1, a2, inTxn) => new FunctionalSignal({ t => fun(a1.now(t), a2.now(t)) }, Iterable(a1, a2), inTxn)
    implicit def signal3[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], Signal[A3], InTxn) => Signal[B] = (a1, a2, a3, inTxn) => new FunctionalSignal({ t => fun(a1.now(t), a2.now(t), a3.now(t)) }, Iterable(a1, a2, a3), inTxn)

    implicit def signalSink1[A](fun: A => Unit): (Signal[A], InTxn) => Unit = (a, inTxn) => { fun(a.now(inTxn)); a.observe(fun)(inTxn) }
    implicit def signalSink2[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], Signal[A2], InTxn) => Unit = (a1, a2, inTxn) => signalSink1(fun.tupled)(new FunctionalSignal({ t => (a1.now(t), a2.now(t)) }, Iterable(a1, a2), inTxn), inTxn)
    implicit def signalSink3[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], Signal[A3], InTxn) => Unit = (a1, a2, a3, inTxn) => signalSink1(fun.tupled)(new FunctionalSignal({ t => (a1.now(t), a2.now(t), a3.now(t)) }, Iterable(a1, a2, a3), inTxn), inTxn)

    implicit def eventStream1[A, B](fun: A => B): (EventStream[A], InTxn) => EventStream[B] = (a, inTxn) => a.map(fun)(inTxn)
    implicit def eventStreamSink1[A](fun: A => Unit): (EventStream[A], InTxn) => Unit = (a, inTxn) => a.observe(fun)(inTxn)

    implicit def eventStream2a[A1, A2, B](fun: (A1, A2) => B): (EventStream[A1], Signal[A2], InTxn) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2], inTxn: InTxn) => ???
    implicit def eventStream2b[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], EventStream[A2], InTxn) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2], inTxn: InTxn) => ???
    implicit def eventStream3a[A1, A2, A3, B](fun: (A1, A2, A3) => B): (EventStream[A1], Signal[A2], Signal[A3], InTxn) => EventStream[B] = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3], inTxn: InTxn) => ???
    implicit def eventStream3b[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], EventStream[A2], Signal[A3], InTxn) => EventStream[B] = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3], inTxn: InTxn) => ???
    implicit def eventStream3c[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], EventStream[A3], InTxn) => EventStream[B] = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3], inTxn: InTxn) => ???

    implicit def eventStreamSink2a[A1, A2](fun: (A1, A2) => Unit): (EventStream[A1], Signal[A2], InTxn) => Unit = (a1: EventStream[A1], a2: Signal[A2], inTxn: InTxn) => ???
    implicit def eventStreamSink2b[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], EventStream[A2], InTxn) => Unit = (a1: Signal[A1], a2: EventStream[A2], inTxn: InTxn) => ???
    implicit def eventStreamSink3a[A1, A2, A3](fun: (A1, A2, A3) => Unit): (EventStream[A1], Signal[A2], Signal[A3], InTxn) => Unit = (a1: EventStream[A1], a2: Signal[A2], a3: Signal[A3], inTxn: InTxn) => ???
    implicit def eventStreamSink3b[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], EventStream[A2], Signal[A3], InTxn) => Unit = (a1: Signal[A1], a2: EventStream[A2], a3: Signal[A3], inTxn: InTxn) => ???
    implicit def eventStreamSink3c[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], EventStream[A3], InTxn) => Unit = (a1: Signal[A1], a2: Signal[A2], a3: EventStream[A3], inTxn: InTxn) => ???
  }
}