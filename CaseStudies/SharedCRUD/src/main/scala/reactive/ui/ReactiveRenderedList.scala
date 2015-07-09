package reactive.ui

import java.awt.Component
import javax.swing.{JList, DefaultListCellRenderer, ListCellRenderer}

import reactive.signals.Signal
import ui.ReactiveList

class ReactiveRenderedList[T](initialElements: Signal[Seq[T]], renderer: Option[RCellRenderer[T]] = None)
  extends ReactiveList(initialElements) {
  renderer match {
    case Some(r) => asComponent.setCellRenderer(new RListCellRenderer[T](r))
    case None =>
  }

  private class RListCellRenderer[T](renderer: RCellRenderer[T]) extends ListCellRenderer[T] {
    val underlyingRender = new DefaultListCellRenderer

    override def getListCellRendererComponent(list: JList[_ <: T], value: T, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
      underlyingRender.getListCellRendererComponent(list, renderer.apply(value), index, isSelected, cellHasFocus)
    }
  }
}
