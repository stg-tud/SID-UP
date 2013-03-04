package reactive.impl

import scala.collection.mutable
import util.Multiset
import commit.Committable
import locks.BinaryTransactionReentrantReadWriteLock
import reactive.Reactive
import reactive.Transaction
import remote.RemoteReactiveDependant
import commit.CountdownAggregateCommitVote
import commit.CommitVote
import java.util.UUID
import scala.actors.threadpool.locks.ReentrantReadWriteLock
import util.LockWithExecute._

abstract class ReactiveImpl[A](val name: String) extends Reactive[A] with Committable[Transaction] {
  val lock = new BinaryTransactionReentrantReadWriteLock[Transaction]()
  private val dependants = mutable.Set[RemoteReactiveDependant[A]]()

  override def addDependant(obs: RemoteReactiveDependant[A]) {
    dependants += obs
  }
  override def removeDependant(obs: RemoteReactiveDependant[A]) {
    dependants -= obs
  }

  private var sourceDependencies = Multiset[UUID]()
  
  private var newSourceDependencies : Multiset[UUID] = _;

  override def commit(tid: Transaction) {
    sourceDependencies = newSourceDependencies
  }

  protected def notifyDependants(transaction: Transaction, commitVote : CommitVote[Transaction], sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A]) {
    if(maybeValue.isDefined || !sourceDependencies.isEmpty) {
      lock.withWriteLockOrVoteNo(transaction, commitVote) {
	    commitVote.registerCommitable(this);
	    newSourceDependencies = sourceDependencies ++ sourceDependenciesDiff
	    val aggregatedDependencyDiff = newSourceDependencies.signum.diff(sourceDependencies.signum)
	    val mustNotify = !dependants.isEmpty && (maybeValue.isDefined || !aggregatedDependencyDiff.isEmpty)
	    
	    if (mustNotify) {
	      if(dependants.size == 1) {
	        dependants.head.notify(transaction, commitVote, aggregatedDependencyDiff, maybeValue);
	      } else {
			  val aggregator = new CountdownAggregateCommitVote(transaction, commitVote, dependants.size + 1);
			  Reactive.executePooledForeach(dependants) { _.notify(transaction, aggregator, aggregatedDependencyDiff, maybeValue) };
	      }
	    } else {
	      commitVote.yes()
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