package ui

import javax.swing.JCheckBox
import java.awt.event.ItemListener
import java.awt.event.ItemEvent
import javax.swing.JComponent
import reactive.Reactive
import reactive.Var

class ReactiveCheckbox(text: Reactive[String], initiallySelected: Boolean = false, enabled: Reactive[Boolean] = Var(true)) extends {
  private val checkbox = new JCheckBox(text.value);
  val asComponent: JComponent = checkbox

} with ReactiveInput[Boolean] {
  checkbox.setSelected(initiallySelected);
  override protected val observeWhileVisible = List(
    observeInEDT(text) { checkbox.setText(_) },
    observeInEDT(enabled) { checkbox.setEnabled(_) });

  val _selected = Var(checkbox.isSelected())
  checkbox.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) = {
      _selected.set(checkbox.isSelected())
    }
  })
  val value: Reactive[Boolean] = _selected

  def setValue(value: Boolean) = checkbox.setSelected(value)
}