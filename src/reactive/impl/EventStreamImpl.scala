package reactive.impl

import scala.collection.mutable
import reactive.EventStream
import reactive.Event
import scala.actors.threadpool.TimeoutException
import reactive.Signal
import reactive.EventStreamDependant
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import util.LockWithExecute._
import reactive.Reactive
import java.util.UUID
import reactive.PropagationData

abstract class EventStreamImpl[A](name: String) extends ReactiveImpl[A](name) with EventStream[A] {
  private val dependencies = mutable.Set[EventStreamDependant[A]]()
  private val dependenciesLock = new ReentrantReadWriteLock;
  override def addDependant(obs: EventStreamDependant[A]) {
    dependenciesLock.writeLocked {
      dependencies += obs
    }
  }
  override def removeDependant(obs: EventStreamDependant[A]) {
    dependenciesLock.writeLocked {
      dependencies -= obs
    }
  }
  protected def notifyDependants(propagationData : PropagationData, maybeValue: Option[A]) {
    dependenciesLock.readLocked {
      Reactive.executePooled(dependencies, { x: EventStreamDependant[A] =>
        x.notifyEvent(propagationData, maybeValue)
      });
    }
  }

  private val valHistory = new mutable.WeakHashMap[Event, Option[A]]();

  protected[this] def maybeNotifyObservers(event: Event, value: Option[A]) {
    valHistory.synchronized {
      valHistory += (event -> value)
      valHistory.notifyAll();
    }
    value.foreach { notifyObservers(event, _); }
  }

  @throws(classOf[TimeoutException])
  override def await(event: Event, timeout: Long = 0): Option[A] = {
    if (!isConnectedTo(event)) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      val end = System.currentTimeMillis() + timeout;
      while (value.isEmpty) {
        if (timeout > 0 && end < System.currentTimeMillis()) throw new TimeoutException(name + " timed out waiting for " + event);
        valHistory.wait(timeout);
        value = valHistory.get(event);
      }
      value
    }.get
  }
  override def hold[B >: A](initialValue: B): Signal[B] = new HoldSignal(this, initialValue);
  override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream((this +: streams): _*);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);
}