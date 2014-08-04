package ui

import javax.swing.JLabel
import reactive.signals.Signal
import reactive.signals.RoutableVar
import reactive.Lift.single.valueToSignal

class ReactiveLabel(initialText: Signal[String]) extends ReactiveComponent(new JLabel(initialText.single.now)) {
  lazy val text = RoutableVar(initialText)
  observeInEDT(text) { asComponent.setText(_) }
}
