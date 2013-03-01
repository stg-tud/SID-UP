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
import remote.RemoteReactive
import scala.actors.threadpool.locks.ReadWriteLock
import reactive.Reactive
import reactive.Transaction
import commit.CommitVote
import remote.RemoteReactiveDependant
import commit.CountdownAggregateCommitVote
import commit.ForkCommitVote
import commit.Committable
import util.Multiset
import commit.Committable
import commit.Committable
import locks.BinaryTransactionReentrantReadWriteLock

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
  private var newSourceDependencies = Multiset[UUID]()
  private var sourceDependenciesDiff = Multiset[UUID]()
  private def finalizeDependenciesDiff = {
    newSourceDependencies = sourceDependencies ++ sourceDependenciesDiff;
    sourceDependenciesDiff = Multiset[UUID]()
    newSourceDependencies.signum -- sourceDependencies.signum
  }
  protected def dependencyChange(transaction: Transaction, commitVote: CommitVote[Transaction], change: Multiset[UUID]) {
    if (!change.isEmpty) {
      if (lock.writeLockOrFail(transaction)) {
        sourceDependenciesDiff ++= change
        commitVote.registerCommitable(this)
      } else {
        commitVote.no()
      }
    }
  }

  override def commit(tid: Transaction) {
    sourceDependencies = newSourceDependencies
  }

  protected def notifyDependants(transaction: Transaction, commitVotes: Iterable[CommitVote[Transaction]], maybeValue: Option[A]) {
    val fork = if (commitVotes.size == 1) commitVotes.head else new ForkCommitVote(commitVotes)
    val commit = new Committable[Transaction] {
      override def commit(tid: Transaction) {

      }
      override def rollback(tid: Transaction) {

      }
    }
    if (dependants.size == 0) {
      fork.yes(commit);
    } else {
      val aggregator = new CountdownAggregateCommitVote(transaction, fork, dependants.size + 1);
      aggregator.yes(commit);
      Reactive.executePooledForeach(dependants) { _.notify(transaction, aggregator, value) };
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