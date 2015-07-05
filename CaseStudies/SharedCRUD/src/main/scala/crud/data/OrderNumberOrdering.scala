package crud.data

import reactive.lifting.Lift
import reactive.signals.Signal
import reactive.sort.ROrdering

class OrderNumberOrdering extends ROrdering[Order] {
  override def compare(a: Order, b: Order): Signal[Int] = {
    Lift.signal2(Ordering[Int].compare)(a.number, b.number)
  }
}
