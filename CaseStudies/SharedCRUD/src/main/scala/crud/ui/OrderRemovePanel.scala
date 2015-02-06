package crud.ui

import java.awt.GridLayout
import javax.swing.{JLabel, JOptionPane, JPanel}

import crud.data.Order
import reactive.Lift._
import reactive.signals.Signal
import ui.{ ReactiveLabel, ReactiveButton }

class OrderRemovePanel(order: Signal[Option[Order]]) extends JPanel {
  protected val removeOrderButton = new ReactiveButton("Remove")

  val removeInstruction = order.pulse(removeOrderButton.commits)
  removeInstruction.filter(_.isEmpty).observe { _ =>
    JOptionPane.showMessageDialog(
      this,
      "Please select an order to delete",
      "No order selected",
      JOptionPane.ERROR_MESSAGE)
  }
  val removeOrders = removeInstruction.collect { case Some(x) => Set(x) }

  // Enable/Disable button depending on whether or not an order is selected
  removeOrderButton.enabled << order.map { _.isDefined }

  // Map selected order number to a label
  val orderNumber = order.map {
    case Some(o) => o.number.now
    case None => "none"
  }
  val orderNumberLabel = new ReactiveLabel(orderNumber)

  setLayout(new GridLayout(2, 2))
  add(new JLabel("You have selected: "))
  add(orderNumberLabel.asComponent)
  add(new JLabel())
  add(removeOrderButton.asComponent)
}
