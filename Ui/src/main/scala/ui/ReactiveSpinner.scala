package ui

import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import java.awt.event.ActionEvent
import javax.swing.JFormattedTextField
import javax.swing.text.DefaultFormatter
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.signals.Var
import reactive.Reactive
import reactive.signals.Signal
import reactive.events.EventSource
import reactive.events.EventStream
import reactive.signals.RoutableSignal
import reactive.Lift.valueToSignal

class ReactiveSpinner(initialValue: Int) extends {
  private val model = new SpinnerNumberModel(initialValue, Int.MinValue, Int.MaxValue, 1);
} with ReactiveComponent(new JSpinner(model)) with ReactiveInput[Int] with ReactiveCommittable {
  lazy val min = {
    val result = RoutableSignal(model.getMinimum().asInstanceOf[Int]);
    observeInEDT(result) { model.setMinimum(_) }
    result
  }
  lazy val max = {
    val result = RoutableSignal(model.getMaximum().asInstanceOf[Int]);
    observeInEDT(result) { model.setMaximum(_) }
    result
  }
  lazy val step = {
    val result = RoutableSignal(model.getStepSize().asInstanceOf[Int]);
    observeInEDT(result) { model.setStepSize(_) }
    result
  }


  private val _value = Var(model.getValue().asInstanceOf[Int])
  model.addChangeListener(new ChangeListener {
    def stateChanged(event: ChangeEvent) = {
      _value << model.getValue().asInstanceOf[Int]
    }
  })
  val value: Signal[Int] = _value
  def setValue(value: Int) = model.setValue(value)

  private val _commits = EventSource[ActionEvent]
  private val editor = asComponent.getEditor().getComponent(0).asInstanceOf[JFormattedTextField]
    editor.addActionListener(new ActionListener() {
      override def actionPerformed(event: ActionEvent) {
        _commits << event;
      }
    })
    val commits: EventStream[ActionEvent] = _commits

  editor.getFormatter().asInstanceOf[DefaultFormatter].setCommitsOnValidEdit(true);
}