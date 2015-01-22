package crud.data

import reactive.signals.Var

case class Order(number: Var[String], date: Var[String]) {
  override def toString = {
    number.now
  }
}
object Order {
  def apply(number: String, date: String): Order = {
    Order(Var(number), Var(date))
  }
}
