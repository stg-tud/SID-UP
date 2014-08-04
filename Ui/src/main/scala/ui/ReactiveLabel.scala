package ui

import javax.swing.JLabel

import reactive.signals.{RoutableVar, Signal}

class ReactiveLabel(initialText: Signal[String]) extends ReactiveComponent(new JLabel(initialText.single.now)) {
  lazy val text = RoutableVar(initialText)
  observeInEDT(text) { asComponent.setText }
}
