package db

import reactive.signals.Var

class Table[A](rows: A*) {
  def select(where: (A) => Boolean): Var[Set[A]] = {
    Var(rows.filter(where).toSet)
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](rows: _*)}
}
