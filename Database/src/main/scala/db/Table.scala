package db

import reactive.events.EventStream
import reactive.signals.{Signal, Var}

class Table[A](rows: Var[Set[A]]) {
  val insertEvents: Var[Set[EventStream[A]]] = Var(Set.empty[EventStream[A]])
  protected val insertEventsTransposedStream: EventStream[Set[A]] = insertEvents.transposeE

  val removeEvents: Var[Set[EventStream[A]]] = Var(Set.empty[EventStream[A]])
  protected val removeEventsTransposedStream: EventStream[Set[A]] = removeEvents.transposeE

  insertEventsTransposedStream.observe { rows << rows.now ++ _ }
  removeEventsTransposedStream.observe { rows << rows.now -- _ }

  def select(where: A => Signal[Boolean]): Signal[Set[A]] = {
    rows.map(_.map(row => where(row).map(_ -> row))).transposeS.map {
      set => set.filter(_._1).map(_._2)
    }
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](Var(rows.toSet))}
}
