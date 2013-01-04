package ui

import javax.swing.JLabel
import javax.swing.JComponent
import reactive.Reactive

class ReactiveLabel(text: Reactive[_]) extends ReactiveComponent {
  private val label = new JLabel(String.valueOf(text.value))
  text.observe { value =>
    label.setText(String.valueOf(value))
  }
  val asComponent: JComponent = label;
}
