package crud.ui

import java.util.Date
import javax.swing.{BoxLayout, JPanel}

import crud.data.Order
import crud.server.RemoteCrud
import reactive.lifting.BooleanLift._
import reactive.lifting.Lift._
import reactive.signals.Signal
import ui.{ReactiveButton, ReactiveDatePicker, ReactiveSpinner}

class OrderAddEditPanel(remoteCrud: RemoteCrud, order: Signal[Option[Order]]) extends JPanel {
  protected val numberInput = new ReactiveSpinner(0)
  protected val dateInput = new ReactiveDatePicker(new Date())
  protected val addOrderButton = new ReactiveButton("Add")
  protected val editOrderButton = new ReactiveButton("Edit")

  protected val nextNumber = numberInput.value.pulse(editOrderButton.commits)
  protected val nextDate = dateInput.value.pulse(editOrderButton.commits)
  
  // Configure numberInput
  numberInput.min << 0

  // Enable/Disable add and edit buttons
  protected def createSelect(selectedNumber: Signal[Int], selectedDate: Signal[Date]) =
    (order: Order) => order.number === selectedNumber && order.date === selectedDate
  protected val selectQuery = remoteCrud.select(createSelect(numberInput.value, dateInput.value))
  protected val duplicateExists = selectQuery.map(_.toSet.size == 0)
  protected val orderSelected = order.map { _.isDefined }
  addOrderButton.enabled << (duplicateExists && !orderSelected)
  editOrderButton.enabled << (duplicateExists && orderSelected)

  order.delta.observe { op =>
    op._1 match {
      case Some(o) => o.number <<- nextNumber ; o.date <<- nextDate
      case None =>
    }
    op._2 match {
      case Some(o) => o.number <<+ nextNumber ; o.date <<+ nextDate
      case None =>
    }
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
