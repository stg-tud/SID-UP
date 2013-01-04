package ui

import java.awt.event.ActionEvent
import javax.swing.JTextField
import java.awt.event.ActionListener
import javax.swing.JComponent
import javax.swing.event.DocumentListener
import javax.swing.event.DocumentEvent
import reactive.Reactive
import reactive.Var

class ReactiveTextField(initialText: String = "", enabled: Reactive[Boolean] = Var(true)) extends ReactiveInput[String] with ReactiveCommitable {
//  private val _commits = EventSource[ActionEvent]
  private val _realTextField = new JTextField(initialText)
  _realTextField.setEnabled(enabled.value)
  enabled.observe { value =>
    _realTextField.setEnabled(value)
  }
//  _realTextField.addActionListener(new ActionListener() {
//    override def actionPerformed(event: ActionEvent) = {
//      _commits << event
//    }
//  })
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

  val value : Reactive[String] = _text
//  val commits: Events[ActionEvent] = _commits
  val asComponent: JComponent = _realTextField
  def setValue(value: String) = _realTextField.setText(value)
}