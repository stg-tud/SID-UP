package ui

import java.awt.event.ActionEvent
import javax.swing.JTextField
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent
import reactive.Reactive
import reactive.signals.Var
import java.awt.EventQueue
import reactive.signals.Signal
import reactive.events.EventStream
import reactive.events.EventSource

class ReactiveTextField() extends ReactiveComponent(new JTextField()) with ReactiveCommittable {
  private val _commits = EventSource[ActionEvent]()
  asComponent.addActionListener(new ActionListener() {
    override def actionPerformed(event: ActionEvent) = {
      _commits << event
    }
  })
  val commits: EventStream[ActionEvent] = _commits

  val _text = Var(asComponent.getText)
  asComponent.getDocument().addDocumentListener(new DocumentListener {
    override def changedUpdate(evt: DocumentEvent): Unit = {
      update()
    }
    override def removeUpdate(evt: DocumentEvent): Unit = {
      update()
    }
    override def insertUpdate(evt: DocumentEvent): Unit = {
      update()
    }
    def update(): Unit = {
      _text << asComponent.getText
    }
  })
  val value: Signal[String] = _text
  def setValue(value: String) = asComponent.setText(value)
}
