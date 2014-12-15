package db


sealed trait TableDelta[A]
case class Insert[A](rows: Set[A]) extends TableDelta[A]
case class Remove[A](rows: Set[A]) extends TableDelta[A]

