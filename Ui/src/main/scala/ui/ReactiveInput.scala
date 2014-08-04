package ui
import reactive.signals.Signal

trait ReactiveInput[A] {
  def value : Signal[A]
  def setValue(value : A): Unit
}
