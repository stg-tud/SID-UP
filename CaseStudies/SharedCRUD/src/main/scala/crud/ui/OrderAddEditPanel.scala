package crud.ui

import java.util.Date
import javax.swing.{BoxLayout, JPanel}

import crud.data.Order
import db.Table
import reactive.lifting.BooleanLift._
import reactive.lifting.Lift._
import reactive.signals.Signal
import ui.{ReactiveButton, ReactiveDatePicker, ReactiveSpinner}

class OrderAddEditPanel(table: Table[Order], order: Signal[Option[Order]]) extends JPanel {
  protected val numberInput = new ReactiveSpinner(0)
  protected val dateInput = new ReactiveDatePicker(new Date())
  protected val addOrderButton = new ReactiveButton("Add")
  protected val editOrderButton = new ReactiveButton("Edit")

  protected val nextNumber = numberInput.value.pulse(editOrderButton.commits)
  protected val nextDate = dateInput.value.pulse(editOrderButton.commits)
  
  // Configure numberInput
  numberInput.min << 0

  // Enable/Disable add and edit buttons
  val duplicateExists = table.count(order => order.number === numberInput.value && order.date === dateInput.value).map(_ == 0)
  val orderSelected = order.map { _.isDefined }
  addOrderButton.enabled << (duplicateExists && !orderSelected)
  editOrderButton.enabled << (duplicateExists && orderSelected)

  val oldOrder = order.delta.mapOption { op => op._1 } observe { order =>
    order.number <<- nextNumber
    order.date <<- nextDate
  }
  val newOrder = order.delta.mapOption { op => op._2 } observe { order =>
    order.number <<+ nextNumber
    order.date <<+ nextDate
  }

  // Assign new order to nextOrders when the add button is pressed
  def makeOrder: (Int, Date) => Order = {(a: Int, b: Date) => Order(a, b)}
  val nextOrders = makeOrder(numberInput.value, dateInput.value).map(Set(_)).pulse(addOrderButton.commits)

  // Configure panel
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  add(numberInput.asComponent)
  add(dateInput.asComponent)
  add(addOrderButton.asComponent)
  add(editOrderButton.asComponent)
}
