package crud.ui

import java.awt.BorderLayout
import java.text.SimpleDateFormat
import javax.swing._

import crud.data.Order
import db.Table

object CrudApp extends App {
  // Setup Table
  val format = new SimpleDateFormat("yyy-MM-dd")
  val table = Table[Order](
    Order(1, format.parse("2015-01-01")),
    Order(2, format.parse("2015-01-02")),
    Order(3, format.parse("2015-01-03"))
  )

  val orderListPanel = new OrderListPanel(table)
  val orderAddEditPanel = new OrderAddEditPanel(table, orderListPanel.selectedOrder)
  val orderRemovePanel = new OrderRemovePanel(orderListPanel.selectedOrder)
  
  // Connect orderAddPanel and orderRemovePanel to table
  table.insertEvents << table.insertEvents.now + orderAddEditPanel.nextOrders
  table.removeEvents << table.removeEvents.now + orderRemovePanel.removeOrders

  // Setup application window
  val window = new JFrame("SharedCRUD Orders")
  window.setLayout(new BorderLayout())
  window.add(orderAddEditPanel, BorderLayout.NORTH)
  window.add(orderListPanel, BorderLayout.CENTER)
  window.add(orderRemovePanel, BorderLayout.SOUTH)
  // window configuration
  window.pack()
  window.setLocationRelativeTo(null)
  window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  window.setVisible(true)
}
