package reactive.lifting

import reactive.signals.Signal
import reactive.lifting.Lift._

object NumericLift {
  implicit class NumericSignal[N](x: Signal[N])(implicit num: Numeric[N]) {
    def +(y: Signal[N]) = signal2(num.plus)(x, y)
    def -(y: Signal[N]) = signal2(num.minus)(x, y)
    def *(y: Signal[N]) = signal2(num.times)(x, y)
    def unary_- = x.map(num.negate)
  }
}
