package crud.ui

import javax.swing.{JOptionPane, BoxLayout, JLabel, JPanel}

import crud.data.Order
import reactive.Lift._
import reactive.events.EventSource
import reactive.signals.Signal
import ui.{ReactiveLabel, ReactiveButton}

class OrderRemovePanel(order: Signal[Option[Order]]) extends JPanel {
  protected val removeOrderButton = new ReactiveButton("Remove")

  val removeOrders = EventSource[Set[Order]]()

  // Assign order to removeOrders when the remove button is pressed
  removeOrderButton.commits.observe { _ => order.now match {
    case Some(o) => removeOrders << Set(o)
    case None => JOptionPane.showMessageDialog(
      this,
      "Please select an order to delete",
      "No order selected",
      JOptionPane.ERROR_MESSAGE
    )
  } }

  // Enable/Disable button depending on whether or not an order is selected
  order.map {
    case Some(_) => removeOrderButton.asComponent.setEnabled(true)
    case None => removeOrderButton.asComponent.setEnabled(false)
  }
  
  // Map selected order number to a label
  val orderNumber = order.map {
    case Some(o) => o.number.now
    case None => "none"
  }
  val orderNumberLabel = new ReactiveLabel(orderNumber)

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(new JLabel("You have selected: "))
  add(orderNumberLabel.asComponent)
  add(removeOrderButton.asComponent)
}
