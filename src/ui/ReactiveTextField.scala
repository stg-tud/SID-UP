package ui

import java.awt.event.ActionEvent
import javax.swing.JTextField
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent
import reactive.Reactive
import reactive.Var
import java.awt.EventQueue
import reactive.Signal
import reactive.EventStream
import reactive.EventSource

class ReactiveTextField(initialText: String = "", enabled: Signal[Boolean] = Var(true)) extends {
  private val _realTextField = new JTextField(initialText)
  override val asComponent: JComponent = _realTextField
} with ReactiveInput[String] with ReactiveCommitable {
  private val _commits = EventSource[ActionEvent]
  override protected val observeWhileVisible = List(observeInEDT(enabled) { _realTextField.setEnabled(_) });
  _realTextField.addActionListener(new ActionListener() {
    override def actionPerformed(event: ActionEvent) = {
      _commits << event
    }
  })
  val _text = Var(_realTextField.getText)
  _realTextField.getDocument().addDocumentListener(new DocumentListener {
    override def changedUpdate(evt: DocumentEvent) {
      update()
    }
    override def removeUpdate(evt: DocumentEvent) {
      update()
    }
    override def insertUpdate(evt: DocumentEvent) {
      update()
    }
    def update() {
      _text.set(_realTextField.getText)
    }
  })

  val value: Signal[String] = _text
  val commits: EventStream[ActionEvent] = _commits
  def setValue(value: String) = _realTextField.setText(value)
}