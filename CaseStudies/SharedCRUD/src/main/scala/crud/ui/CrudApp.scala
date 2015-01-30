package crud.ui

import java.awt.BorderLayout
import javax.swing._

import crud.data.Order
import db.Table

object CrudApp extends App {
  // Setup Table
  val table = Table[Order](
    Order("1", "2015-01-01"),
    Order("2", "2015-01-02"),
    Order("3", "2015-01-03")
  )

  val orderAddPanel = new OrderAddPanel()
  val orderListPanel = new OrderListPanel(table)
  val orderRemovePanel = new OrderRemovePanel(orderListPanel.selectedOrder)
  
  // Connect orderAddPanel and orderRemovePanel to table
  table.insertEvents << table.insertEvents.now + orderAddPanel.nextOrders
  table.removeEvents << table.removeEvents.now + orderRemovePanel.removeOrders

  // Setup application window
  val window = new JFrame("SharedCRUD Orders")
  window.setLayout(new BorderLayout())
  window.add(orderAddPanel, BorderLayout.NORTH)
  window.add(orderListPanel, BorderLayout.CENTER)
  window.add(orderRemovePanel, BorderLayout.SOUTH)
  // window configuration
  window.pack()
  window.setLocationRelativeTo(null)
  window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  window.setVisible(true)
}
