package ui

import javax.swing.JCheckBox
import java.awt.event.ItemListener
import java.awt.event.ItemEvent
import javax.swing.JComponent
import reactive.Reactive
import reactive.Var

class ReactiveCheckbox(text: Reactive[String], initiallySelected: Boolean = false, enabled: Reactive[Boolean] = Var(true)) extends ReactiveInput[Boolean] {
  private val checkbox = new JCheckBox(text.value);
  checkbox.setSelected(initiallySelected);
  text.observe { value =>
    AWTThreadSafe {
      checkbox.setText(value)
    }
  }
  checkbox.setEnabled(enabled.value)
  enabled.observe { value =>
    AWTThreadSafe {
      checkbox.setEnabled(value)
    }
  }

  val _selected = Var(checkbox.isSelected())
  checkbox.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) = {
      _selected.set(checkbox.isSelected())
    }
  })
  val value: Reactive[Boolean] = _selected

  val asComponent: JComponent = checkbox
  def setValue(value: Boolean) = checkbox.setSelected(value)
}