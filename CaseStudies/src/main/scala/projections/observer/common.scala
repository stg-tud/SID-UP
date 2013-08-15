package projections.observer


trait Observer[I] {
  def receive(v: I): Unit
}

trait Observable[I] {
  def notifyObservers(v: I)
}

case class Message[V](value: V, sender: String, direct: Boolean = false)
