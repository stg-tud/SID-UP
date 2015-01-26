package crud.ui

import javax.swing.{BoxLayout, JPanel}

import crud.data.Order
import reactive.Lift._
import reactive.events.EventSource
import ui.{ReactiveButton, ReactiveTextField}

class OrderAddPanel extends JPanel {
  protected val numberTextField = new ReactiveTextField()
  protected val dateTextField = new ReactiveTextField()
  protected val addOrderButton = new ReactiveButton("Add")

  numberTextField.setValue("Number")
  dateTextField.setValue("Date")

  val nextOrders = EventSource[Set[Order]]()

  // Assign new order to nextOrders when the add button is pressed
  addOrderButton.commits.observe { _ => nextOrders << Set(Order(numberTextField.value.now, dateTextField.value.now)) }

  // Configure panel
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(numberTextField.asComponent)
  add(dateTextField.asComponent)
  add(addOrderButton.asComponent)
}
