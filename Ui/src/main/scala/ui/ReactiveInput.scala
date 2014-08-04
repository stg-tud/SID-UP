package ui
import javax.swing.JComponent
import reactive.Reactive
import reactive.signals.Signal

trait ReactiveInput[A] {
  def value : Signal[A]
  def setValue(value : A): Unit
}
