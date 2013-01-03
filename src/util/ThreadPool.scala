package util

import scala.collection.mutable
import scala.actors.threadpool.AtomicInteger

class ThreadPool(numThreads: Int) {
  private val lock = new Object();
  private var numWorkers = 0;

  def execute(op: => Unit) {
    lock.synchronized {
      numWorkers += 1;
    }
    new Thread(new Runnable() {
      override def run() {
        op;
        lock.synchronized {
          numWorkers -= 1;
          if (numWorkers == 0) lock.notifyAll();
        }
      }
    }).start();
  }

  def awaitCompletion() {
    lock.synchronized {
      while (numWorkers > 0) {
        lock.wait();
      }
    }
  }
}