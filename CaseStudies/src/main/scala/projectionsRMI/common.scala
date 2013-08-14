package projectionsRMI

@remote trait RemoteObservable[I] {
  def addObserver(o: Observer[I]): Unit
}

@remote trait Observer[I] {
  def receive(v: I): Unit
}

trait Observable[I] {
  var observers = List[Observer[I]]()
  def addObserver(o: Observer[I]) = observers ::= o
  def notifyObservers(v: I) = observers.foreach(_.receive(v))
}

case class Message[V](value: V, sender: String, direct: Boolean = false)
