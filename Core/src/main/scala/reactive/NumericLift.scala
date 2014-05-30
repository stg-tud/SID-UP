package reactive

import reactive.signals.Signal
import scala.concurrent.stm.InTxn

object NumericLift {
  object single {
    implicit class NumericSignal[N](x: Signal[N])(implicit num: Numeric[N]) {
      def +(y: Signal[N]) = Lift.single.signal2(num.plus)(x, y)
      def -(y: Signal[N]) = Lift.single.signal2(num.minus)(x, y)
      def *(y: Signal[N]) = Lift.single.signal2(num.times)(x, y)
      def unary_- = x.single.map(num.negate)
    }
  }
  object transactional {
    implicit class NumericSignal[N](x: Signal[N])(implicit num: Numeric[N]) {
      def +(y: Signal[N])(implicit inTxn: InTxn) = Lift.transactional.signal2(num.plus)(x, y, inTxn)
      def -(y: Signal[N])(implicit inTxn: InTxn) = Lift.transactional.signal2(num.minus)(x, y, inTxn)
      def *(y: Signal[N])(implicit inTxn: InTxn) = Lift.transactional.signal2(num.times)(x, y, inTxn)
      def unary_- = x.single.map(num.negate)
    }
  }
}
