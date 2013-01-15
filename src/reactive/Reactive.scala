package reactive

import scala.collection.mutable
import scala.concurrent.ops.spawn
import util.ToStringHelper._
import util.Util._
import java.util.UUID
import scala.concurrent.ThreadPoolRunner
import scala.actors.threadpool.Executor
import scala.actors.scheduler.ExecutorScheduler
import scala.actors.threadpool.Executors
import scala.actors.threadpool.ExecutorService
import scala.collection.mutable.Stack

abstract class Reactive[A](val name: String, private var currentValue: A) {
  protected[reactive] val dependencies: mutable.MutableList[ReactiveDependant] = mutable.MutableList()
  def addDependant(obs: ReactiveDependant) {
    dependencies += obs
  }
  def sourceDependencies: Map[UUID, UUID]
  //  protected[reactive] def level: Int;

  // TODO: instead of using a WeakHashMap, references on events should be counted explicitly.
  // Using a WeakHashMap works, but retains events unnecessarily long, which irrevokably bloats
  // each map's size. That is however a bunch of work, especially considering there can exist
  // multiple instances of the "same" event through back and forth network transfers
  private val valHistory = new mutable.WeakHashMap[Event, A]();

  def value = {
    val currentEvent = Reactive.threadEvent.get();
    valHistory.synchronized { valHistory.get(currentEvent) }.getOrElse(currentValue);
  }

  def await(event: Event) = {
    if((event.sourcesAndPredecessors.keySet & sourceDependencies.keySet).isEmpty) {
      throw new IllegalArgumentException("illegal wait: "+event+" will not update this reactive.");
    }
    valHistory.synchronized {
      var value = valHistory.get(event);
      while (value.isEmpty) {
        valHistory.wait();
        value = valHistory.get(event);
      }
      value
    }.get
  }

  protected[this] def updateValue(event: Event, newValue: A) {
    val changed = !nullSafeEqual(currentValue, newValue);
    currentValue = newValue;

    valHistory.synchronized {
      valHistory += (event -> newValue)
      valHistory.notifyAll();
    }

    if (changed) observers.foreach { _.notify(newValue) }
    notifyDependencies(event, changed)
  }
  protected[this] def notifyDependencies(event: Event, changed: Boolean): Unit = {
    dependencies.foreach { x =>
      Reactive.executeNotifyPooled(x, event, changed)
    }
  }

  // ====== Observing stuff ======

  private class ObserverHandler(name: String, op: A => Unit) {
    def notify(value: A) = op(value)
    override def toString = name
  }
  private val observers: mutable.MutableList[ObserverHandler] = mutable.MutableList()
  def observe(obs: => Unit) {
    observe(_ => obs)
  }
  def observe(name: String, obs: => Unit) {
    observe(name)(_ => obs)
  }
  def observe(obs: A => Unit) {
    observe(obs.getClass().getName())(obs)
  }
  def observe(name: String)(obs: A => Unit) {
    observers += new ObserverHandler(name, obs);
  }

  def dirty: Reactive[Boolean]

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

object Reactive {
  private val threadEvent = new ThreadLocal[Event]() {
    override def initialValue = {
      null
    }
  }
  def during[A](event: Event)(op: => A) = {
    val old = threadEvent.get();
    threadEvent.set(event);
    val result = op;
    threadEvent.set(old);
    result;
  }

  implicit def autoSignalToValue[A](signal: Reactive[A]): A = signal.value

  private val lock = new Object();
  private var pool: ExecutorService = null;
  def withThreadPoolSize[A](size : Int)(op : => A) : A = {
    lock.synchronized {
      if(pool != null) throw new IllegalStateException("Someone else is already using the thread pool!");
      setThreadPoolSize(size);
    }
    try {
      op;
    } finally {
      setThreadPoolSize(0);
    }
  }
  def setThreadPoolSize(size: Int) {
    lock.synchronized {
      if (pool != null) {
        pool.shutdown()
      }
      if (size > 0) {
        pool = Executors.newFixedThreadPool(size)
      } else {
        pool = null
      }
    }
  }
  private def executeNotifyPooled(dependent: ReactiveDependant, event: Event, changed: Boolean) {
    lock.synchronized {
      if (pool == null) {
        dependent.notifyUpdate(event, changed)
      } else {
        pool.execute(new Runnable {
          override def run() {
            dependent.notifyUpdate(event, changed)
          }
        })
      }
    }
  }
}