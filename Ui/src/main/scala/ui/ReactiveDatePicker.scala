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
import reactive.signals.RoutableVar
import reactive.lifting.Lift.valueToSignal
import org.jdesktop.swingx.JXDatePicker
import java.util.Date
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeEvent

class ReactiveDatePicker(initialValue: Date) extends {
  private val picker = new JXDatePicker(initialValue)
} with ReactiveComponent(picker) with ReactiveInput[Date] with ReactiveCommittable {
  private val _value = Var(picker.getDate)
  picker.addPropertyChangeListener("date", new PropertyChangeListener {
    def propertyChange(event: PropertyChangeEvent) = {
      _value << picker.getDate
    }
  })
  val value: Signal[Date] = _value
  def setValue(value: Date) = picker.setDate(value)

  private val _commits = EventSource[ActionEvent]
  private val editor = asComponent.getEditor().getComponent(0).asInstanceOf[JFormattedTextField]
    editor.addActionListener(new ActionListener() {
      override def actionPerformed(event: ActionEvent): Unit = {
        _commits << event;
      }
    })
    val commits: EventStream[ActionEvent] = _commits

  editor.getFormatter().asInstanceOf[DefaultFormatter].setCommitsOnValidEdit(true);
}