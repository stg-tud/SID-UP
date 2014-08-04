package ui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JFormattedTextField, JSpinner, SpinnerNumberModel}
import javax.swing.event.{ChangeEvent, ChangeListener}
import javax.swing.text.DefaultFormatter

import reactive.Lift.single.valueToSignal
import reactive.events.{EventSource, EventStream}
import reactive.signals.{RoutableVar, Signal, Var}

class ReactiveSpinner(initialValue: Int) extends {
  private val model = new SpinnerNumberModel(initialValue, Int.MinValue, Int.MaxValue, 1)
} with ReactiveComponent(new JSpinner(model)) with ReactiveInput[Int] with ReactiveCommittable {
  lazy val min = {
    val routableVar = RoutableVar(model.getMinimum().asInstanceOf[Int])
    observeInEDT(routableVar) { model.setMinimum(_) }
    routableVar
  }
  lazy val max = {
    val routableVar = RoutableVar(model.getMaximum().asInstanceOf[Int])
    observeInEDT(routableVar) { model.setMaximum(_) }
    routableVar
  }
  lazy val step = {
    val routableVar = RoutableVar(model.getStepSize().asInstanceOf[Int])
    observeInEDT(routableVar) { model.setStepSize(_) }
    routableVar
  }


  private val _value = Var(model.getValue().asInstanceOf[Int])
  model.addChangeListener(new ChangeListener {
    def stateChanged(event: ChangeEvent) = {
      _value << model.getValue().asInstanceOf[Int]
    }
  })
  val value: Signal[Int] = _value
  def setValue(value: Int) = model.setValue(value)

  private val _commits = EventSource[ActionEvent]()
  private val editor = asComponent.getEditor().getComponent(0).asInstanceOf[JFormattedTextField]
    editor.addActionListener(new ActionListener() {
      override def actionPerformed(event: ActionEvent): Unit = {
        _commits << event
      }
    })
    val commits: EventStream[ActionEvent] = _commits

  editor.getFormatter().asInstanceOf[DefaultFormatter].setCommitsOnValidEdit(true)
}
