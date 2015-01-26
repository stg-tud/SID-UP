package crud.ui

import java.awt.BorderLayout
import javax.swing._

import crud.data.Order
import db.Table
import ui.ReactiveLabel

object CrudApp extends App {
  // Setup Table
  val table = Table[Order](
    Order("1", "2015-01-01"),
    Order("2", "2015-01-02"),
    Order("3", "2015-01-03")
  )

  val orderAddPanel = new OrderAddPanel()
  val orderListPanel = new OrderListPanel(table)
  
  // Connect addOrderPanel to table
  table.insertEvents << table.insertEvents.now + orderAddPanel.nextOrders

  // Map selected order number to a label
  val selectedOrderNumber = orderListPanel.selectedOrder.map {
    case Some(order) => order.number.now
    case None => "none"
  }
  val selectedOrderNumberLabel = new ReactiveLabel(selectedOrderNumber)

  // Setup application window
  val window = new JFrame("SharedCRUD Orders")
  window.setLayout(new BorderLayout())
  window.add(orderAddPanel, BorderLayout.NORTH)
  window.add(orderListPanel, BorderLayout.CENTER)
  val output = new Box(BoxLayout.X_AXIS)
  output.add(new JLabel("You have selected: "))
  output.add(selectedOrderNumberLabel.asComponent)
  window.add(output, BorderLayout.SOUTH)
  // window configuration
  window.pack()
  window.setLocationRelativeTo(null)
  window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  window.setVisible(true)
}
