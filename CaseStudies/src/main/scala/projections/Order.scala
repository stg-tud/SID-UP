package projections

import reactive.signals.Signal
import reactive.Lift._
import java.util.Date

case class Order[N](cost: N)(implicit num: Numeric[N]) {
	val createdOn = new Date
	override def toString = s"Order(${cost})"
}
