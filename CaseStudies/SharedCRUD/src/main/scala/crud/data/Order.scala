package crud.data

import java.text.SimpleDateFormat
import java.util.{UUID, Date}

import reactive.signals.SettableSignal

case class Order(number: SettableSignal[Int], date: SettableSignal[Date]) {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

  override def toString = {
    number.now + ": " + dateFormat.format(date.now)
  }

  // TODO this is a workaround to allow equals to work with remote references
  val uuid = UUID.randomUUID()
  override def equals(other: Any): Boolean = {
    if (other.isInstanceOf[Order]) {
        return other.asInstanceOf[Order].uuid == this.uuid
    }
    super.equals(other)
  }
}
object Order {
  def apply(number: Int, date: Date): Order = {
    new Order(SettableSignal(number), SettableSignal(date))
  }
}
