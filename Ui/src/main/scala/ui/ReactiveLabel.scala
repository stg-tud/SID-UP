package ui

import javax.swing.JLabel
import reactive.signals.Signal
import reactive.signals.RoutableVar
import reactive.Lift.valueToSignal

class ReactiveLabel(initialText: Signal[String]) extends ReactiveComponent(new JLabel()) {
  lazy val text = RoutableVar(initialText);
  observeInEDT(text) { asComponent.setText(_) }
}
