package reactive.impl

import scala.collection.mutable
import scala.concurrent.ops.spawn
import util.ToStringHelper._
import util.Util._
import util.LockWithExecute._
import java.util.UUID
import scala.concurrent.ThreadPoolRunner
import scala.actors.threadpool.Executor
import scala.actors.scheduler.ExecutorScheduler
import scala.actors.threadpool.Executors
import scala.actors.threadpool.ExecutorService
import scala.collection.mutable.Stack
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import remote.RemoteEventStream
import scala.actors.threadpool.locks.ReadWriteLock
import reactive.Reactive
import reactive.Event
import reactive.EventStreamDependant

abstract class ReactiveImpl[A](val name: String) extends Reactive[A] {

  def sourceDependencies: Map[UUID, UUID]
  //  protected[reactive] def level: Int;

  // ====== Observing stuff ======

  private val observers = mutable.Set[A => Unit]()
  private val observersLock = new ReentrantReadWriteLock
  def observe(obs: A => Unit) {
    observersLock.writeLocked {
      observers += obs
    }
  }
  def unobserve(obs: A => Unit) {
    observersLock.writeLocked {
      observers -= obs
    }
  }

  protected def notifyObservers(event: Event, value: A) {
    observersLock.readLocked {
      observers.foreach { _(value) }
    }
  }

  override def toString = name
  // ====== Printing stuff ======
  //
  //  override def toString = name;
  //  def toElaborateString: String = {
  //    return toString(new StringBuilder(), 0, new java.util.HashSet[Reactive[_]]).toString;
  //  }
  //  def toString(builder: StringBuilder, depth: Int, done: java.util.Set[Reactive[_]]): StringBuilder = {
  //    indent(builder, depth).append("<").append(getClass().getSimpleName().toLowerCase());
  //    if (done.add(this)) {
  //      builder.append(" name=\"").append(name) /*.append("\" level=\"").append(level)*/ .append("\">\n");
  //      listTag(builder, depth + 1, "observers", observers) {
  //        x => indent(builder, depth + 2).append("<observer>").append(x.toString()).append("</observer>\n");
  //      }
  //      listTag(builder, depth + 1, "dependencies", dependencies) {
  //        _.toString(builder, depth + 2, done);
  //      }
  //    } else {
  //      builder.append(" backref=\"").append(name).append("\"/>\n");
  //    }
  //    return builder;
  //  }
}