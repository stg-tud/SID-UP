package reactive.remote

import java.rmi.Naming

import reactive.events.EventStream
import reactive.remote.impl._
import reactive.signals.Signal


object RemoteReactives {
  def makeRemote[A](signal: Signal[A]) = new RemoteSignalSourceImpl(signal)

  def makeRemote[A](event: EventStream[A]) = new RemoteEventSourceImpl(event)

  def makeLocal[A](dependency: RemoteDependency[A]): EventStream[A] = new RemoteEventSinkImpl(dependency)

  def makeLocal[A](dependency: RemoteSignalDependency[A]): Signal[A] = new RemoteSignalSinkImpl(dependency)

  def rebind[A](name: String, signal: Signal[A]): Unit = Naming.rebind(name, makeRemote(signal))

  def rebind[A](name: String, event: EventStream[A]): Unit = Naming.rebind(name, makeRemote(event))

  def lookupSignal[A](name: String): Signal[A] = makeLocal(Naming.lookup(name).asInstanceOf[RemoteSignalDependency[A]])

  def lookupEvent[A](name: String): EventStream[A] = makeLocal(Naming.lookup(name).asInstanceOf[RemoteDependency[A]])
}
