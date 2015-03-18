package crud.data

import java.text.SimpleDateFormat
import java.util.Date
import reactive.signals.{Signal, Var}

case class Order(number: Signal[Int], date: Signal[Date]) {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  override def toString = {
    number.now + ": " + dateFormat.format(date.now)
  }
}
object Order {
  def apply(number: Int, date: Date): Order = {
    Order(Var(number), Var(date))
  }
}
