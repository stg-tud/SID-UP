package db

import reactive.signals.{Signal, Var}

class Table[A](rows: A*) {
  def select(where: (A) => Signal[Boolean]): Var[Set[A]] = {
    Var(rows.filter {where(_).now}.toSet)
  }
}

object Table {
  def apply[A](rows: A*): Table[A] = {new Table[A](rows: _*)}
}
