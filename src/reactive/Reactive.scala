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
trait Reactive[+A] extends RemoteReactive[A] {
  val name: String;
  def log : Signal[List[A]]
  // TODO should be package protected 
  def sourceDependencies: Map[UUID, UUID]
  // TODO should be package protected 
  def isConnectedTo(event: Event): Boolean = !(event.sourcesAndPredecessors.keySet & sourceDependencies.keySet).isEmpty
  // TODO should be package protected 
  def observe(obs: A => Unit)
  // TODO should be package protected 
  def unobserve(obs: A => Unit)
}

object Reactive {
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
  def executePooled(op: => Unit) {
    lock.synchronized {
      if (pool == null) {
        op
      } else {
        pool.execute(new Runnable {
          override def run() {
            op
          }
        })
      }
    }
  }
  def executePooled[A](dependencies: Iterable[A], op: A => Unit) {
    if (!dependencies.isEmpty) {
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
}