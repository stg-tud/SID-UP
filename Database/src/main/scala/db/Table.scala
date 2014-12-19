package db

import reactive.events.{EventSource, EventStream}
import reactive.signals.{Signal, Var}

class Table[A](rows: Var[Iterable[A]]) {
  protected val imperativeInsertEvents = EventSource[Iterable[A]]()
  protected val imperativeRemoveEvents = EventSource[Iterable[A]]()

  val insertEvents: Var[Seq[EventStream[Iterable[A]]]] = Var(Seq(imperativeInsertEvents))
  val removeEvents: Var[Seq[EventStream[Iterable[A]]]] = Var(Seq(imperativeRemoveEvents))

  protected val transposedInsertEvents: EventStream[Iterable[A]] = insertEvents.transposeE.map { _.flatten }
  protected val transposedRemoveEvents: EventStream[Iterable[A]] = removeEvents.transposeE.map { _.flatten }

  protected val insertDeltaEvents = transposedInsertEvents.map { Insert(_) }
  protected val removeDeltaEvents = transposedRemoveEvents.map { Remove(_) }

  protected val allEvents = insertDeltaEvents merge removeDeltaEvents

  protected val internalRows = allEvents.fold[Iterable[A]](rows.now) { (rows, delta) => delta match {
    case Insert(insertRows) => rows ++ insertRows
    case Remove(removeRows) => rows.filterNot(removeRows.toSet)
  }}

  def select(where: A => Signal[Boolean]): Signal[Iterable[A]] = {
    internalRows.map(_.map(row => where(row).map(_ -> row))).transposeS.map {
      set => set.filter(_._1).map(_._2)
    }
  }

  def insert(rows: A*): Unit = {
    imperativeInsertEvents << rows
  }

  def remove(rows: A*): Unit = {
    imperativeRemoveEvents << rows
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](Var(rows.toSet))}
}
