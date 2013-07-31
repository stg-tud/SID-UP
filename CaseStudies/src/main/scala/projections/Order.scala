package projections

import reactive.signals.Signal
import reactive.Lift._

case class Order[N](cost: N)(implicit num: Numeric[N]) {
	override def toString = s"Order(${cost})"
}
