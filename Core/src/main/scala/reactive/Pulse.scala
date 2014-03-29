package reactive

import java.util.UUID

case class Pulse[+P](value: Option[P] = None, sourceDependencies: Option[Set[UUID]] = None) {
  def map[Q](f: P => Q) = copy(value = value.map(f))
  def hasChanged: Boolean = value.isDefined
  def filter(p: P => Boolean) = copy(value = value.filter(p))
}

object Pulse {
  val noChange = new Pulse()
  def change[P](value: P) = new Pulse(Some(value))
}
