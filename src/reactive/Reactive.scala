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
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import remote.RemoteReactive

/**
 *  Note: while this class implements a remote interface, it doesn't actually
 *  provide remoting capabilities for performance reasons. The interface is
 *  implemented only to provide a remote-capable wrapper to just forward all
 *  method invocations.
 */
abstract class Reactive[A](val name: String, private var currentValue: A) extends RemoteReactive[A] {
  private val dependencies = mutable.Set[ReactiveDependant[_ >: A]]()
  private val dependenciesLock = new ReentrantReadWriteLock;
  override def addDependant(obs: ReactiveDependant[_ >: A]) {
    dependenciesLock.writeLock().lock();
    dependencies += obs
    dependenciesLock.writeLock().unlock();
  }
  override def removeDependant(obs: ReactiveDependant[_ >: A]) {
    dependenciesLock.writeLock().lock();
    dependencies -= obs
    dependenciesLock.writeLock().unlock();
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

  /**
   * suspends the current thread until this reactive has completed processing the given event.
   */
  def await(event: Event) = {
    if ((event.sourcesAndPredecessors.keySet & sourceDependencies.keySet).isEmpty) {
      throw new IllegalArgumentException("illegal wait: " + event + " will not update this reactive.");
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
    val changed = valHistory.synchronized {
      val changed = !nullSafeEqual(currentValue, newValue);
      currentValue = newValue;

      valHistory += (event -> newValue)
      valHistory.notifyAll();
      changed
    }

    if (changed) {
      observersLock.readLock().lock();
      observers.foreach { _(newValue) }
      observersLock.readLock().unlock();
    }
    dependenciesLock.readLock().lock();

    Reactive.executeNotifyPooled[A](dependencies, { x: ReactiveDependant[_ >: A] =>
      if (changed) {
        x.notifyUpdate(event, newValue)
      } else {
        x.notifyEvent(event)
      }
    });
    dependenciesLock.readLock().unlock();
  }

  // ====== Observing stuff ======

  private val observers = mutable.Set[A => Unit]()
  private val observersLock = new ReentrantReadWriteLock
  def observe(obs: A => Unit) {
    observersLock.writeLock().lock()
    observers += obs
    observersLock.writeLock().unlock()
  }
  def unobserve(obs: A => Unit) {
    observersLock.writeLock().lock()
    observers -= obs
    observersLock.writeLock().unlock()
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
    try {
      op
    } finally {
      threadEvent.set(old);
    }
  }

  implicit def autoSignalToValue[A](signal: Reactive[A]): A = signal.value

  private val lock = new Object();
  private var pool: ExecutorService = null;
  def withThreadPoolSize[A](size: Int)(op: => A): A = {
    lock.synchronized {
      if (pool != null) throw new IllegalStateException("Someone else is already using the thread pool!");
      setThreadPoolSize(size);
    }
    try {
      op;
    } finally {
      setThreadPoolSize(0);
    }
  }
  /**
   * use with care, somebody else might be using his own pool already. Supply 0 to shut down the current pool.
   */
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
  private def executeNotifyPooled[A](dependencies: Iterable[ReactiveDependant[_ >: A]], op: ReactiveDependant[_ >: A] => Unit) {
    lock.synchronized {
      if (pool == null) {
        dependencies.foreach { x => op(x) }
      } else {
        dependencies.foreach { x =>
          pool.execute(new Runnable {
            override def run() {
              op(x)
            }
          })
        }
      }
    }
  }
}