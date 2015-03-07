package crud.data

import reactive.signals.{Signal, Var}

case class Order(number: Signal[String], date: Signal[String]) {
  override def toString = {
    number.now + ": " + date.now
  }
}
object Order {
  def apply(number: String, date: String): Order = {
    Order(Var(number), Var(date))
  }
}
