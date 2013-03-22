package reactive.impl

import scala.collection.mutable
import reactive.Signal
import reactive.EventStream
import reactive.Reactive._
import reactive.Transaction
import remote.RemoteReactiveDependant
import dctm.vars.TransactionExecutor
import util.Multiset
import java.util.UUID

class SnapshotSignal[A](signal: Signal[A], events: EventStream[_], t: Txn) extends SignalImpl[A]("snapshot(" + signal.name + ")on(" + events.name + ")", signal.now) {

  private val lock = new Object();
  private val waitingForEventStream = mutable.Map[Transaction, A]()
  private val waitingForSignal = mutable.Set[Transaction]()
  private val ignoreForSignal = mutable.Set[Transaction]()

  signal.addDependant(new RemoteReactiveDependant[A] {
    override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
      val tid = t.tid.uuid;
      if (sourceDependencies.get.get(tid) > 1) {
        val shouldEmit = lock.synchronized {
          if (ignoreForSignal.remove(tid)) {
            false
          } else {
            val shouldEmit = waitingForSignal.remove(event);
            if (!shouldEmit) {
              waitingForEventStream += (event -> value);
            }
            shouldEmit
          }
        }
        if (shouldEmit) {
          propagate(event, Some(value));
        }
      }
    }
  });
  events.addDependant(new RemoteReactiveDependant[Any] {
    override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
      maybeValue match {
        case None =>
          propagate(event, None);
          if (signal.isConnectedTo(event)) {
            lock.synchronized {
              if (waitingForEventStream.remove(event).isEmpty) {
                ignoreForSignal += event;
              }
            }
          }
        case Some(_) =>
          if (signal.isConnectedTo(event)) {
            lock.synchronized {
              waitingForEventStream.remove(event) match {
                case Some(value) => propagate(event, Some(value));
                case None => waitingForSignal += event
              }
            }
          } else {
            propagate(event, Some(signal.now));
          }
      }
    }
  });
}