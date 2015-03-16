package crud.data

import java.util.Date
import reactive.signals.{Signal, Var}

case class Order(number: Signal[Int], date: Signal[Date]) {
  override def toString = {
    number.now + ": " + date.now
  }
}
object Order {
  def apply(number: Int, date: Date): Order = {
    Order(Var(number), Var(date))
  }
}
