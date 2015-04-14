package reactive.remote

import java.rmi.Naming

import reactive.signals.Signal
import reactive.events.EventStream
import reactive.remote.impl._


object RemoteReactives {
  def rebind[A](name: String, signal: Signal[A]): Unit = Naming.rebind(name, new RemoteSignalPublisher(signal))

  def rebind[A](name: String, event: EventStream[A]): Unit = Naming.rebind(name, new RemoteEventStreamPublisher(event))

  def lookupSignal[A](name: String): Signal[A] = new RemoteSignalSubscriber(Naming.lookup(name).asInstanceOf[RemoteSignalDependency[A]])

  def lookupEvent[A](name: String): EventStream[A] = new RemoteEventStreamSubscriber(Naming.lookup(name).asInstanceOf[RemoteDependency[A]])
}
