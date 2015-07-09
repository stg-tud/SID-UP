package reactive.ui

trait RCellRenderer[T] {
  def apply(element: T): String
}
