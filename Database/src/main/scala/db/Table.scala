package db

import reactive.events.{ EventSource, EventStream }
import reactive.signals.{ Signal, Var }
import reactive.signals.Val

class Table[A](rows: Var[Set[A]]) {
  protected val imperativeInsertEvents = EventSource[Set[A]]()
  protected val imperativeRemoveEvents = EventSource[Set[A]]()

  val insertEvents: Var[Set[EventStream[Set[A]]]] = Var(Set(imperativeInsertEvents))
  val removeEvents: Var[Set[EventStream[Set[A]]]] = Var(Set(imperativeRemoveEvents))

  protected val transposedInsertEvents: EventStream[Set[A]] = insertEvents.transposeE.map { _.flatten }
  protected val transposedRemoveEvents: EventStream[Set[A]] = removeEvents.transposeE.map { _.flatten }

  protected val insertDeltaEvents = transposedInsertEvents.map { Insert(_) }
  protected val removeDeltaEvents = transposedRemoveEvents.map { Remove(_) }

  protected val allEvents = insertDeltaEvents merge removeDeltaEvents

  protected val internalRows = allEvents.fold[Set[A]](rows.now) { (rows, delta) =>
    delta match {
      case Insert(insertRows) => rows ++ insertRows
      case Remove(removeRows) => rows.filterNot(removeRows.toSet)
    }
  }

  def select(where: A => Signal[Boolean] = Table.all): Signal[Set[A]] = {
    if (where eq Table.all) {
      internalRows
    } else {
      internalRows.map(_.map(row => where(row).map(_ -> row))).transposeS.map {
        set => set.filter(_._1).map(_._2)
      }
    }
  }

  def insert(rows: A*): Unit = {
    imperativeInsertEvents << rows.toSet
  }

  def remove(rows: A*): Unit = {
    imperativeRemoveEvents << rows.toSet
  }
}

object Table {
  val all = { _: Any => Val(true) }
  def apply[A](rows: A*): Table[A] = { new Table[A](Var(rows.toSet)) }
}
