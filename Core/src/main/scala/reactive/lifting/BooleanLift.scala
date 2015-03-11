package reactive.lifting

import reactive.signals.Signal
import Lift._

object BooleanLift {
  implicit class BooleanSignal(x: Signal[Boolean]) {
    def &&(y: Signal[Boolean]) = signal2({ (a: Boolean, b: Boolean) => a && b })(x, y)
    def ||(y: Signal[Boolean]) = signal2({ (a: Boolean, b: Boolean) => a || b })(x, y)
    def unary_! = x.map(!_)
  }
}
