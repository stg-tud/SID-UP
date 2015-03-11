package db

import reactive.events.{ EventSource, EventStream }
import reactive.signals.{ Signal, Var }
import reactive.signals.Val

class Table[A](rows: Var[Set[A]]) {
  type Elements = Set[A]
  type Where = A => Boolean
  type RWhere = A => Signal[Boolean]

  protected val imperativeInsertEvents = EventSource[Elements]()
  protected val imperativeRemoveWhereEvents = EventSource[Where]()

  val insertEvents: Var[Set[EventStream[Elements]]] = Var(Set(imperativeInsertEvents))
  val removeEvents: Var[Set[EventStream[Where]]] = Var(Set(imperativeRemoveWhereEvents))

  protected val transposedInsertEvents: EventStream[Elements] = insertEvents.transposeE.map { _.flatten }
  protected val transposedRemoveWhereEvents: EventStream[Where] = removeEvents.transposeE.map(wheres =>
    if (wheres.size == 1) {
      wheres.head
    } else {
      { element: A => wheres.find(_(element)).isDefined }
    })

  protected val insertDeltaEvents = transposedInsertEvents.map { elements => { inserts: Elements => elements ++ inserts } }
  protected val removeDeltaEvents = transposedRemoveWhereEvents.map { where => { elements: Elements => elements.filterNot(where) } }

  protected val allEvents = insertDeltaEvents merge removeDeltaEvents

  protected val internalRows = allEvents.fold[Elements](rows.now) { (rows, delta) => delta(rows) }

  def select(where: RWhere = Table.all): Signal[Elements] = {
    if (where eq Table.all) {
      internalRows
    } else {
      internalRows.map(_.map(row => where(row).map(_ -> row))).transposeS.map {
        _.filter(_._1).map(_._2)
      }
    }
  }

  def count(where: RWhere = Table.all): Signal[Int] = select(where) map (_.size)

  def insert(rows: A*): Unit = {
    imperativeInsertEvents << rows.toSet
  }

  def remove(rows: A*): Unit = {
    remove(rows.toSet)
  }

  def remove(where: Where): Unit = {
    imperativeRemoveWhereEvents << where
  }
}

object Table {
  val all = { _: Any => Val(true) }
  def apply[A](rows: A*): Table[A] = { new Table[A](Var(rows.toSet)) }
}
