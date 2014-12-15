package db

import reactive.events.EventStream
import reactive.signals.{Signal, Var}

class Table[A](rows: Var[Set[A]]) {
  val insertEvents: Var[Set[EventStream[A]]] = Var(Set.empty[EventStream[A]])
  protected val insertEventsTransposedStream: EventStream[TableDelta[A]] = insertEvents.transposeE.map { Insert(_) }

  val removeEvents: Var[Set[EventStream[A]]] = Var(Set.empty[EventStream[A]])
  protected val removeEventsTransposedStream: EventStream[TableDelta[A]] = removeEvents.transposeE.map { Remove(_) }

  protected val allEvents = insertEventsTransposedStream.merge(removeEventsTransposedStream)
  protected val internalRows = allEvents.fold[Set[A]](rows.now) {(rows, delta) => delta match {
    case Insert(a) => rows ++ a
    case Remove(a) => rows -- a
  }}

  def select(where: A => Signal[Boolean]): Signal[Set[A]] = {
    internalRows.map(_.map(row => where(row).map(_ -> row))).transposeS.map {
      set => set.filter(_._1).map(_._2)
    }
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](Var(rows.toSet))}
}
