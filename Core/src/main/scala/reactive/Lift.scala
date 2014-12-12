package reactive

import reactive.events.EventStream
import reactive.signals.{ Signal, Val }
import reactive.signals.impl.FunctionalSignal
import scala.concurrent.stm._
import scala.language.implicitConversions
import reactive.events.impl.TuplePulseEventStream1
import reactive.events.impl.TuplePulseEventStream2

object Lift {
  implicit def valueToSignal[A](value: A): Signal[A] = new Val(value)

  implicit def signal1[A1, B](fun: A1 => B): Signal[A1] => Signal[B] = a => a.map(fun)
  implicit def signal2[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], Signal[A2]) => Signal[B] = (a1, a2) => atomic { transactional.signal2(fun)(a1, a2, _) }
  implicit def signal3[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], Signal[A3]) => Signal[B] = (a1, a2, a3) => atomic { transactional.signal3(fun)(a1, a2, a3, _) }
  implicit def signal4[A1, A2, A3, A4, B](fun: (A1, A2, A3, A4) => B): (Signal[A1], Signal[A2], Signal[A3], Signal[A4]) => Signal[B] = (a1, a2, a3, a4) => atomic { transactional.signal4(fun)(a1, a2, a3, a4, _) }

  implicit def signalSink1[A](fun: A => Unit): Signal[A] => Unit = a => atomic { transactional.signalSink1(fun)(a, _) }
  implicit def signalSink2[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], Signal[A2]) => Unit = (a1, a2) => atomic { transactional.signalSink2(fun)(a1, a2, _) }
  implicit def signalSink3[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], Signal[A3]) => Unit = (a1, a2, a3) => atomic { transactional.signalSink3(fun)(a1, a2, a3, _) }

  implicit def eventStream1[A, B](fun: A => B): EventStream[A] => EventStream[B] = a => a.map(fun)
  implicit def eventStreamSink1[A](fun: A => Unit): EventStream[A] => Unit = a => a.observe(fun)

  implicit def eventStream2a[A1, A2, B](fun: (A1, A2) => B): (EventStream[A1], Signal[A2]) => EventStream[B] = (e: EventStream[A1], a2: Signal[A2]) => atomic { tx => transactional.eventStream2a(fun)(e, a2, tx) }
  implicit def eventStream2b[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], EventStream[A2]) => EventStream[B] = (a1: Signal[A1], e: EventStream[A2]) => atomic { tx => transactional.eventStream2b(fun)(a1, e, tx) }
  implicit def eventStream3a[A1, A2, A3, B](fun: (A1, A2, A3) => B): (EventStream[A1], Signal[A2], Signal[A3]) => EventStream[B] = (e: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => atomic { tx => transactional.eventStream3a(fun)(e, a2, a3, tx) }
  implicit def eventStream3b[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], EventStream[A2], Signal[A3]) => EventStream[B] = (a1: Signal[A1], e: EventStream[A2], a3: Signal[A3]) => atomic { tx => transactional.eventStream3b(fun)(a1, e, a3, tx) }
  implicit def eventStream3c[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], EventStream[A3]) => EventStream[B] = (a1: Signal[A1], a2: Signal[A2], e: EventStream[A3]) => atomic { tx => transactional.eventStream3c(fun)(a1, a2, e, tx) }

  implicit def eventStreamSink2a[A1, A2](fun: (A1, A2) => Unit): (EventStream[A1], Signal[A2]) => Unit = (e: EventStream[A1], a2: Signal[A2]) => atomic { tx => transactional.eventStreamSink2a(fun)(e, a2, tx) }
  implicit def eventStreamSink2b[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], EventStream[A2]) => Unit = (a1: Signal[A1], e: EventStream[A2]) => atomic { tx => transactional.eventStreamSink2b(fun)(a1, e, tx) }
  implicit def eventStreamSink3a[A1, A2, A3](fun: (A1, A2, A3) => Unit): (EventStream[A1], Signal[A2], Signal[A3]) => Unit = (e: EventStream[A1], a2: Signal[A2], a3: Signal[A3]) => atomic { tx => transactional.eventStreamSink3a(fun)(e, a2, a3, tx) }
  implicit def eventStreamSink3b[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], EventStream[A2], Signal[A3]) => Unit = (a1: Signal[A1], e: EventStream[A2], a3: Signal[A3]) => atomic { tx => transactional.eventStreamSink3b(fun)(a1, e, a3, tx) }
  implicit def eventStreamSink3c[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], EventStream[A3]) => Unit = (a1: Signal[A1], a2: Signal[A2], e: EventStream[A3]) => atomic { tx => transactional.eventStreamSink3c(fun)(a1, a2, e, tx) }

  object transactional {
    implicit def valueToSignal[A](value: A): Signal[A] = new Val(value)

    implicit def signal1[A1, B](fun: A1 => B): (Signal[A1], InTxn) => Signal[B] = (a, inTxn) => a.transactional.map(fun)(inTxn)
    implicit def signal2[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], Signal[A2], InTxn) => Signal[B] = (a1, a2, inTxn) => new FunctionalSignal({ t => fun(a1.transactional.now(t), a2.transactional.now(t)) }, Set(a1, a2), inTxn)
    implicit def signal3[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], Signal[A3], InTxn) => Signal[B] = (a1, a2, a3, inTxn) => new FunctionalSignal({ t => fun(a1.transactional.now(t), a2.transactional.now(t), a3.transactional.now(t)) }, Set(a1, a2, a3), inTxn)
    implicit def signal4[A1, A2, A3, A4, B](fun: (A1, A2, A3, A4) => B): (Signal[A1], Signal[A2], Signal[A3], Signal[A4], InTxn) => Signal[B] = (a1, a2, a3, a4, inTxn) => new FunctionalSignal({ t => fun(a1.transactional.now(t), a2.transactional.now(t), a3.transactional.now(t), a4.transactional.now(t)) }, Set(a1, a2, a3, a4), inTxn)

    implicit def signalSink1[A](fun: A => Unit): (Signal[A], InTxn) => Unit = (a, inTxn) => { fun(a.transactional.now(inTxn)); a.transactional.observe(fun)(inTxn) }
    implicit def signalSink2[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], Signal[A2], InTxn) => Unit = (a1, a2, inTxn) => signalSink1(fun.tupled)(new FunctionalSignal({ t => (a1.transactional.now(t), a2.transactional.now(t)) }, Set(a1, a2), inTxn), inTxn)
    implicit def signalSink3[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], Signal[A3], InTxn) => Unit = (a1, a2, a3, inTxn) => signalSink1(fun.tupled)(new FunctionalSignal({ t => (a1.transactional.now(t), a2.transactional.now(t), a3.transactional.now(t)) }, Set(a1, a2, a3), inTxn), inTxn)

    implicit def eventStream1[A, B](fun: A => B): (EventStream[A], InTxn) => EventStream[B] = (a, inTxn) => a.transactional.map(fun)(inTxn)
    implicit def eventStreamSink1[A](fun: A => Unit): (EventStream[A], InTxn) => Unit = (a, inTxn) => a.transactional.observe(fun)(inTxn)

    implicit def eventStream2a[A1, A2, B](fun: (A1, A2) => B): (EventStream[A1], Signal[A2], InTxn) => EventStream[B] = (e: EventStream[A1], a2: Signal[A2], inTxn: InTxn) => new TuplePulseEventStream1(e, a2, inTxn).transactional.map(fun.tupled)(inTxn)
    implicit def eventStream2b[A1, A2, B](fun: (A1, A2) => B): (Signal[A1], EventStream[A2], InTxn) => EventStream[B] = (a1: Signal[A1], e: EventStream[A2], inTxn: InTxn) => new TuplePulseEventStream1(e, a1, inTxn).transactional.map(t => fun(t._2, t._1))(inTxn)
    implicit def eventStream3a[A1, A2, A3, B](fun: (A1, A2, A3) => B): (EventStream[A1], Signal[A2], Signal[A3], InTxn) => EventStream[B] = (e: EventStream[A1], a2: Signal[A2], a3: Signal[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a2, a3, inTxn).transactional.map(fun.tupled)(inTxn)
    implicit def eventStream3b[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], EventStream[A2], Signal[A3], InTxn) => EventStream[B] = (a1: Signal[A1], e: EventStream[A2], a3: Signal[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a1, a3, inTxn).transactional.map(t => fun(t._2, t._1, t._3))(inTxn)
    implicit def eventStream3c[A1, A2, A3, B](fun: (A1, A2, A3) => B): (Signal[A1], Signal[A2], EventStream[A3], InTxn) => EventStream[B] = (a1: Signal[A1], a2: Signal[A2], e: EventStream[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a1, a2, inTxn).transactional.map(t => fun(t._2, t._3, t._1))(inTxn)

    implicit def eventStreamSink2a[A1, A2](fun: (A1, A2) => Unit): (EventStream[A1], Signal[A2], InTxn) => Unit = (e: EventStream[A1], a2: Signal[A2], inTxn: InTxn) => new TuplePulseEventStream1(e, a2, inTxn).transactional.observe(fun.tupled)(inTxn)
    implicit def eventStreamSink2b[A1, A2](fun: (A1, A2) => Unit): (Signal[A1], EventStream[A2], InTxn) => Unit = (a1: Signal[A1], e: EventStream[A2], inTxn: InTxn) => new TuplePulseEventStream1(e, a1, inTxn).transactional.map(t => (t._2, t._1))(inTxn).transactional.observe(fun.tupled)(inTxn)
    implicit def eventStreamSink3a[A1, A2, A3](fun: (A1, A2, A3) => Unit): (EventStream[A1], Signal[A2], Signal[A3], InTxn) => Unit = (e: EventStream[A1], a2: Signal[A2], a3: Signal[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a2, a3, inTxn).transactional.observe(fun.tupled)(inTxn)
    implicit def eventStreamSink3b[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], EventStream[A2], Signal[A3], InTxn) => Unit = (a1: Signal[A1], e: EventStream[A2], a3: Signal[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a1, a3, inTxn).transactional.observe(t => fun(t._2, t._1, t._3))(inTxn)
    implicit def eventStreamSink3c[A1, A2, A3](fun: (A1, A2, A3) => Unit): (Signal[A1], Signal[A2], EventStream[A3], InTxn) => Unit = (a1: Signal[A1], a2: Signal[A2], e: EventStream[A3], inTxn: InTxn) => new TuplePulseEventStream2(e, a1, a2, inTxn).transactional.observe(t => fun(t._2, t._3, t._1))(inTxn)
  }
}
