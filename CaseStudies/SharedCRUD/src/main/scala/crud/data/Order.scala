package crud.data

import java.text.SimpleDateFormat
import java.util.Date

import reactive.signals.SettableSignal

case class Order(number: SettableSignal[Int], date: SettableSignal[Date]) {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  override def toString = {
    number.now + ": " + dateFormat.format(date.now)
  }
}
object Order {
  def apply(number: Int, date: Date): Order = {
    new Order(SettableSignal(number), SettableSignal(date))
  }
}
