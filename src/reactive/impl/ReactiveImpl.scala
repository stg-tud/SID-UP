package reactive
package impl

import Reactive._
import util.Multiset
import java.util.UUID
import dctm.vars.TransactionalVariable
import remote.RemoteReactiveDependant
import dctm.vars.TransactionExecutor
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import scala.collection.mutable
import util.LockWithExecute._

abstract class ReactiveImpl[A](val name: String) extends Reactive[A] {
  private val dependants = new TransactionalVariable[Set[RemoteReactiveDependant[A]], Transaction](Set());

  override def addDependant(obs: RemoteReactiveDependant[A])(implicit t: Txn) = {
    dependants.transform { _ + obs }
    sourceDependencies.get.signum
  }
  override def removeDependant(obs: RemoteReactiveDependant[A])(implicit t: Txn) = {
    dependants.transform { _ - obs }
    sourceDependencies.get.signum
  }

  private val sourceDependencies = new TransactionalVariable[Map[UUID, Set[UUID]], Transaction](Map())
  
  protected def getExpectedNotificationCount(implicit t : Txn) = {
    val deps = sourceDependencies.get();
    t.tid.sources.foldLeft(Set[UUID]()) { (set, source) =>
      set ++ deps(source)
    }.size
  }

  protected def notifyDependants(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
    if (maybeValue.isDefined || !sourceDependenciesDiff.isEmpty) {
      val sourceDependencyChange = sourceDependencies.transform { _ ++ sourceDependenciesDiff }
      val aggregatedDependencyDiff = sourceDependencyChange._2.signum.diff(sourceDependencyChange._1.signum)

      val dependants = this.dependants.get()
      val mustNotify = !dependants.isEmpty && (maybeValue.isDefined || !aggregatedDependencyDiff.isEmpty)

      if (mustNotify) {
        if (dependants.size == 1) {
          dependants.head.notify(aggregatedDependencyDiff, maybeValue);
        } else {
          TransactionExecutor.spawnSubtransactions(dependants) { _.notify(aggregatedDependencyDiff, maybeValue) };
        }
      }
    }
  }

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

  protected def notifyObservers(event: Transaction, value: A) {
    observersLock.readLocked {
      observers.foreach { _(value) }
    }
  }

  override def toString = name
}