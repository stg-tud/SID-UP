package crud.ui

import java.awt.BorderLayout
import javax.swing._

import crud.data.Order
import db.Table
import ui.{ReactiveLabel, ReactiveList}

object CrudApp extends App {
  // Setup Table
  val table = Table[Order](
    Order("1", "2015-01-01"),
    Order("2", "2015-01-02"),
    Order("3", "2015-01-03")
  )

  // List order functionality
  val list = new ReactiveList[Order](table.select().map(_.toList))
  val displayText = list.selectionOption.map {
    case Some(order) => order.number.now
    case None => "none"
  }
  val label = new ReactiveLabel(displayText)

  val window = new JFrame("SharedCRUD Orders")
  window.setLayout(new BorderLayout())
  window.add(new JLabel("Please select an order from this list:"), BorderLayout.NORTH)
  window.add(new JScrollPane(list.asComponent), BorderLayout.CENTER)
  val output = new Box(BoxLayout.X_AXIS)
  output.add(new JLabel("You have selected: "))
  output.add(label.asComponent)
  window.add(output, BorderLayout.SOUTH)
  // window configuration
  window.pack()
  window.setLocationRelativeTo(null)
  window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  window.setVisible(true)
}
