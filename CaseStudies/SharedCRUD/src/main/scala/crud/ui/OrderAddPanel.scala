package crud.ui

import java.util.Date
import javax.swing.{BoxLayout, JPanel}

import crud.data.Order
import db.Table
import reactive.lifting.BooleanLift._
import reactive.lifting.Lift._
import ui.{ReactiveButton, ReactiveDatePicker, ReactiveSpinner}

class OrderAddPanel(table: Table[Order]) extends JPanel {
  protected val numberInput = new ReactiveSpinner(0)
  protected val dateInput = new ReactiveDatePicker(new Date())
  protected val addOrderButton = new ReactiveButton("Add")
  
  // Configure numberInput
  numberInput.min << 0

  // Prevent duplicate numbers
  addOrderButton.enabled << table.count(order => order.number === numberInput.value && order.date === dateInput.value).map(_ == 0)

  // Assign new order to nextOrders when the add button is pressed
  def makeOrder: (Int, Date) => Order = {(a: Int, b: Date) => Order(a, b)}
  val nextOrders = makeOrder(numberInput.value, dateInput.value).map(Set(_)).pulse(addOrderButton.commits)

  // Configure panel
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(numberInput.asComponent)
  add(dateInput.asComponent)
  add(addOrderButton.asComponent)
}
