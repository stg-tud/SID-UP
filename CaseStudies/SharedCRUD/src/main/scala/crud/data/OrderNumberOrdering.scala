package crud.data

class OrderNumberOrdering extends Ordering[Order] {
  override def compare(x: Order, y: Order): Int = {
    x.number.now.toInt - y.number.now.toInt
  }
}
