package ui

import java.awt.event.ActionEvent
import javax.swing.JButton
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.signals.Signal
import reactive.events.EventSource
import reactive.events.EventStream
import reactive.signals.RoutableSignal
import reactive.Lift.valueToSignal

class ReactiveButton(initialText: Signal[String]) extends ReactiveComponent(new JButton()) with ReactiveCommittable {
  val text = RoutableSignal(initialText)
  observeInEDT(text) { asComponent.setText(_) };

  private val _clicks = EventSource[ActionEvent]
  asComponent.addActionListener(new ActionListener() {
    override def actionPerformed(event: ActionEvent) = {
      _clicks << event
    }
  })
  override val commits: EventStream[ActionEvent] = _clicks
}