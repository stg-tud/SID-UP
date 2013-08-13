package reactive

import reactive.signals.Val
import reactive.signals.impl.FunctionalSignal
import reactive.signals.Signal
import Numeric.Implicits._

object NumericLift {
  class SignalStaysNumeric[N: Numeric] extends Numeric[Signal[N]] {
    def plus(x: Signal[N], y: Signal[N]): Signal[N] = new FunctionalSignal({ t => x.value(t) + y.value(t) }, x, y)
    def minus(x: Signal[N], y: Signal[N]): Signal[N] = new FunctionalSignal({ t => x.value(t) - y.value(t) }, x, y)
    def times(x: Signal[N], y: Signal[N]): Signal[N] = new FunctionalSignal({ t => x.value(t) * y.value(t) }, x, y)
    def negate(x: Signal[N]): Signal[N] = new FunctionalSignal({ t => -x.value(t) }, x)
    def fromInt(x: Int): Signal[N] = new Val(implicitly[Numeric[N]].fromInt(x))
    def toInt(x: Signal[N]): Int = x.now.toInt
    def toLong(x: Signal[N]): Long = x.now.toLong
    def toFloat(x: Signal[N]): Float = x.now.toFloat
    def toDouble(x: Signal[N]): Double = x.now.toDouble
    def compare(x: Signal[N], y: Signal[N]): Int = implicitly[Numeric[N]].compare(x.now, y.now)
  }
  implicit def theMethodForNiceSyntaxOrSomethingLikeThat[N: Numeric]: Numeric[Signal[N]] = new SignalStaysNumeric[N]
}
