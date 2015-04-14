package crud.ui

import java.awt.BorderLayout
import javax.swing.{JPanel, JScrollPane}

import crud.data.{Order, OrderNumberOrdering}
import reactive.signals.Signal
import ui.ReactiveList

class OrderListPanel(selectQuery: Signal[Set[Order]]) extends JPanel {
  protected val orderList = new ReactiveList[Order](selectQuery.map { _.toList.sorted(new OrderNumberOrdering()) })
  val selectedOrder = orderList.selectionOption

  // Span list over whole panel
  setLayout(new BorderLayout(0, 0))
  add(new JScrollPane(orderList.asComponent))
}
