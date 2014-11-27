package db

import reactive.signals.{Signal, Var}
import reactive.signals.TransposeSignal

class Table[A](rows: Var[Set[A]]) {
  def select(where: A => Signal[Boolean]): Signal[Set[A]] = {
    rows.map(_.map(row => where(row).map(_ -> row))).transposeS
    
    // TODO
    ???
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](Var(rows.toSet))}
}
