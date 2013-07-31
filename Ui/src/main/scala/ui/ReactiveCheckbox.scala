package ui

import javax.swing.JCheckBox
import java.awt.event.ItemListener
import java.awt.event.ItemEvent
import javax.swing.JComponent
import reactive.Reactive
import reactive.signals.Var
import reactive.signals.Signal
import reactive.signals.RoutableVar
import reactive.Lift.valueToSignal

class ReactiveCheckbox(initialText: Signal[String]) extends ReactiveComponent(new JCheckBox()) with ReactiveInput[Boolean] {
  val text = RoutableVar(initialText)
  observeInEDT(text) { asComponent.setText(_) };

  private val _selected = Var(asComponent.isSelected())
  asComponent.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) = {
      _selected << asComponent.isSelected()
    }
  })
  override val value: Signal[Boolean] = _selected
  override def setValue(value: Boolean) = asComponent.setSelected(value)
}