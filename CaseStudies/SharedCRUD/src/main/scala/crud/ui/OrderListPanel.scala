package crud.ui

import java.awt.BorderLayout
import javax.swing.{JPanel, JScrollPane}

import crud.data.{Order, OrderNumberOrdering}
import reactive.signals.Signal
import reactive.sort.RSort
import ui.ReactiveList

class OrderListPanel(selectQuery: Signal[Set[Order]]) extends JPanel {
  implicit val ordering = new OrderNumberOrdering
  val sort = new RSort[Order]()
  
  val sortedList = sort.rrsort(selectQuery.map(_.toList))
  protected val orderList = new ReactiveList[Order](sortedList)
  val selectedOrder = orderList.selectionOption

  // Span list over whole panel
  setLayout(new BorderLayout(0, 0))
  add(new JScrollPane(orderList.asComponent))
}
