package projections

import java.util.Date

case class Order[N: Numeric](value: N) {
  val createdOn = new Date
  override def toString = s"Order(${value})"
}
