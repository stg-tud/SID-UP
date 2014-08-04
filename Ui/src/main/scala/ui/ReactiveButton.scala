package ui

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.JButton

import reactive.events.{EventSource, EventStream}
import reactive.signals.{RoutableVar, Signal}

class ReactiveButton(initialText: Signal[String]) extends ReactiveComponent(new JButton()) with ReactiveCommittable {
  val text = RoutableVar(initialText)
  observeInEDT(text) { asComponent.setText }

  private val _clicks = EventSource[ActionEvent]()
  asComponent.addActionListener(new ActionListener() {
    override def actionPerformed(event: ActionEvent) = {
      _clicks << event
    }
  })
  override val commits: EventStream[ActionEvent] = _clicks
}
