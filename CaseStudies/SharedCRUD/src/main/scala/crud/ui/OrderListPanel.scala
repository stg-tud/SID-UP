package crud.ui

import java.awt.BorderLayout
import javax.swing._

import crud.data.{Order, OrderNumberOrdering}
import reactive.signals.Signal
import reactive.sort.RSort
import reactive.ui.{ReactiveRenderedList, RCellRenderer}

class OrderListPanel(selectQuery: Signal[Set[Order]]) extends JPanel {
  implicit val ordering = new OrderNumberOrdering
  val sort = new RSort[Order]()

  val sortedList = sort.rrsort(selectQuery.map(_.toList))
  val sortedStringList = sortedList.map(_.map(_.toString))
  protected val orderList = new ReactiveRenderedList[Order](sortedList, Some(new OrderCellRenderer))
  val selectedOrder = orderList.selectionOption

  // Span list over whole panel
  setLayout(new BorderLayout(0, 0))
  add(new JScrollPane(orderList.asComponent))
}

class OrderCellRenderer extends RCellRenderer[Order] {
  override def apply(element: Order): Signal[String] = {
    (element.date.changes merge element.number.changes).map(_ => element.toString).hold(element.toString)
  }
}
