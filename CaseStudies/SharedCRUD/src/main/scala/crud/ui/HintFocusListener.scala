package crud.ui

import java.awt.event.{FocusEvent, FocusListener}
import javax.swing.JTextField

class HintFocusListener(
    val textField: JTextField,
    val initial: String
) extends FocusListener {
  override def focusGained(e: FocusEvent): Unit = {
    if (textField.getText == initial) {
      textField.setText("")
    }
  }

  override def focusLost(e: FocusEvent): Unit = {
    if (textField.getText == "") {
      textField.setText(initial)
    }
  }
}
