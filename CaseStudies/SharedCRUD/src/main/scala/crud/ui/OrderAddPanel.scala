package crud.ui

import javax.swing.{BoxLayout, JPanel}

import crud.data.Order
import db.Table
import reactive.lifting.Lift._
import reactive.lifting.BooleanLift._
import reactive.events.EventSource
import ui.{ReactiveButton, ReactiveTextField}

class OrderAddPanel(table: Table[Order]) extends JPanel {
  protected val initialNumber = "Number"
  protected val initialDate = "Date"
  protected val numberTextField = new ReactiveTextField()
  protected val dateTextField = new ReactiveTextField()
  protected val addOrderButton = new ReactiveButton("Add")

  numberTextField.setValue(initialNumber)
  dateTextField.setValue(initialDate)

  numberTextField.asComponent.addFocusListener(new HintFocusListener(numberTextField.asComponent, initialNumber))
  dateTextField.asComponent.addFocusListener(new HintFocusListener(dateTextField.asComponent, initialDate))

  // Prevent duplicate numbers
  addOrderButton.enabled << table.count(order => order.number === numberTextField.value && order.date === dateTextField.value).map(_ == 0)

  // Assign new order to nextOrders when the add button is pressed
  def makeOrder: (String, String) => Order = {(a: String, b: String) => Order(a, b)}
  val nextOrders = makeOrder(numberTextField.value, dateTextField.value).map(Set(_)).pulse(addOrderButton.commits)

  // Configure panel
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(numberTextField.asComponent)
  add(dateTextField.asComponent)
  add(addOrderButton.asComponent)
}
