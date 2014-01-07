package projections

import java.util.Date

case class Order(value: Int) {
  val createdOn = new Date
  override def toString = s"Order(${value})"
}
