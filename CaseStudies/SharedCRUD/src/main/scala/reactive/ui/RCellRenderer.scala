package reactive.ui

import reactive.signals.Signal

trait RCellRenderer[T] {
  def apply(element: T): Signal[String]
}
