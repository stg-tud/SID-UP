package ui

import java.awt.event.{ItemEvent, ItemListener}
import javax.swing.JCheckBox

import reactive.signals.{RoutableVar, Signal, Var}

class ReactiveCheckbox(initialText: Signal[String]) extends ReactiveComponent(new JCheckBox()) with ReactiveInput[Boolean] {
  val text = RoutableVar(initialText)
  observeInEDT(text) { asComponent.setText }

  private val _selected = Var(asComponent.isSelected())
  asComponent.addItemListener(new ItemListener {
    def itemStateChanged(event: ItemEvent) = {
      _selected << asComponent.isSelected()
    }
  })
  override val value: Signal[Boolean] = _selected
  override def setValue(value: Boolean) = asComponent.setSelected(value)
}
