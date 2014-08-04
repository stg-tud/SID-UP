package ui

import javax.swing.{DefaultListModel, JList, ListSelectionModel}
import javax.swing.event.{ListSelectionEvent, ListSelectionListener}

import reactive.signals.{RoutableVar, Signal, Var}

import scala.collection.JavaConversions._

class ReactiveList[T](initialElements: Signal[List[T]]) extends {
  val model = new DefaultListModel[T]()
} with ReactiveComponent(new JList(model)) with ReactiveInput[Option[T]] {
  asComponent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  val elements = RoutableVar(initialElements)
  observeInEDT(elements) { newElements =>
    model.removeAllElements()
    newElements.foreach { model.addElement };
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
