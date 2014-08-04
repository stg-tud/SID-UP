package ui

import reactive.signals.Signal
import javax.swing.JList
import javax.swing.ListModel
import javax.swing.DefaultListModel
import javax.swing.AbstractListModel
import reactive.signals.RoutableVar
import javax.swing.event.ListSelectionListener
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import reactive.signals.Var
import scala.collection.JavaConversions._

class ReactiveList[T](initialElements: Signal[List[T]]) extends {
  val model = new DefaultListModel[T]()
} with ReactiveComponent(new JList(model)) with ReactiveInput[Option[T]] {
  asComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  val elements = RoutableVar(initialElements)
  observeInEDT(elements) { newElements =>
    model.removeAllElements()
    newElements.foreach { model.addElement(_) };
  }
  private val _selection = Var(asComponent.getSelectedValuesList().toList.headOption)
  asComponent.addListSelectionListener(new ListSelectionListener() {
    override def valueChanged(event: ListSelectionEvent): Unit = {
      if (!event.getValueIsAdjusting()) {
        _selection << asComponent.getSelectedValuesList().toList.headOption
      }
    }
  })
  override val value: Signal[Option[T]] = _selection
  val selectionOption = value

  override def setValue(value: Option[T]) = {
    value match {
      case None => asComponent.clearSelection()
      case Some(value) => asComponent.setSelectedValue(value, true);
    }
  }
}
