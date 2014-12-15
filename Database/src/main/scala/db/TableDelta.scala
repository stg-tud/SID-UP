package db


sealed trait TableDelta[A]
case class Insert[A](rows: A*) extends TableDelta[A]
case class Remove[A](rows: A*) extends TableDelta[A]

