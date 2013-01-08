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
  protected[reactive] val dependencies: mutable.MutableList[DependantReactive[_]] = mutable.MutableList()
  def addDependant(obs: DependantReactive[_]) {
    dependencies += obs
  }
  def sourceDependencies: Iterable[UUID]
  //  protected[reactive] def level: Int;

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

  // TODO: For a real distributed setting, releasing events
  // must be done only under agreement of all machines
  // instead of local-machine-only weak references.
  // Otherwise, a cached value can be deleted, but then the
  // event gets re-instantiated by a second remote invocation
  // which will then fail to find the cached value, default
  // to using the current value and thus cause a glitch for
  // that event.
  private val valHistory = mutable.WeakHashMap[Event, A]();
  def value = {
    valHistory.get(Reactive.threadEvent.get()) match {
      case Some(x) => x
      case None => currentValue
    }
  }

  protected[this] def updateValue(event: Event, newValue: A) {
    val changed = !nullSafeEqual(currentValue, newValue);
    if (changed) {
      valHistory += (event -> newValue)
      currentValue = newValue;
      observers.foreach { _.notify(newValue) }
    }
    notifyDependencies(event, changed)
  }
  protected[this] def notifyDependencies(event: Event, changed: Boolean): Unit = {
    dependencies.foreach { x =>
      Reactive.executeNotifyPooled(x, event, changed)
    }
  }

  override def toString = name;
  def toElaborateString: String = {
    return toString(new StringBuilder(), 0, new java.util.HashSet[Reactive[_]]).toString;
  }
  def toString(builder: StringBuilder, depth: Int, done: java.util.Set[Reactive[_]]): StringBuilder = {
    indent(builder, depth).append("<").append(getClass().getSimpleName().toLowerCase());
    if (done.add(this)) {
      builder.append(" name=\"").append(name) /*.append("\" level=\"").append(level)*/ .append("\">\n");
      listTag(builder, depth + 1, "observers", observers) {
        x => indent(builder, depth + 2).append("<observer>").append(x.toString()).append("</observer>\n");
      }
      listTag(builder, depth + 1, "dependencies", dependencies) {
        _.toString(builder, depth + 2, done);
      }
    } else {
      builder.append(" backref=\"").append(name).append("\"/>\n");
    }
    return builder;
  }
}

object Reactive {
  private val threadEvent = new ThreadLocal[Event]() {
    override def initialValue = {
      null
    }
  }
  def during[A](event: Event)(op: => A) = {
    threadEvent.set(event);
    val result = op;
    threadEvent.remove();
    result;
  }

  implicit def autoSignalToValue[A](signal: Reactive[A]): A = signal.value

  private val lock = new Object();
  private var pool: ExecutorService = null;
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
  private def executeNotifyPooled(dependent: DependantReactive[_], event: Event, changed: Boolean) {
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