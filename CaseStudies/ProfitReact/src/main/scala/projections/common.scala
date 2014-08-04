package projections

import java.util.Date

import scala.language.implicitConversions

case class Order(value: Int) {
  val createdOn = new Date
  override def toString = s"Order($value)"
}

sealed abstract class Participant(val name: String)
object Participant {
  implicit def asString(p: Participant) = p.name
}
case object client extends Participant("client")
case object sales extends Participant("sales")
case object purchases extends Participant("purchases")
case object management extends Participant("management")
