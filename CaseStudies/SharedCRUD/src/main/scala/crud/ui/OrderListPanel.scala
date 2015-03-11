package crud.ui

import java.awt.BorderLayout
import javax.swing.{JScrollPane, JPanel}

import crud.data.{OrderNumberOrdering, Order}
import db.Table
import ui.ReactiveList

class OrderListPanel(table: Table[Order]) extends JPanel {
  protected val orderList = new ReactiveList[Order](table.select().map(_.toList.sorted(new OrderNumberOrdering())) )
  val selectedOrder = orderList.selectionOption

  // Span list over whole panel
  setLayout(new BorderLayout(0, 0))
  add(new JScrollPane(orderList.asComponent))
}
