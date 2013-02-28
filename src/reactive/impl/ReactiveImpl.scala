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
import locks.TransactionReentrantReadWriteLock
import commit.CommitVote
import commit.DelegateCountdownAggregateCommitVote
import remote.RemoteReactiveDependant
import commit.ForkCommitVote

abstract class ReactiveImpl[A](val name: String) extends Reactive[A] {
  val lock = new TransactionReentrantReadWriteLock[Transaction]()

  private val dependencies = mutable.Set[RemoteReactiveDependant[A]]()

  override def addDependant(obs: RemoteReactiveDependant[A]) {
    dependencies += obs
  }
  override def removeDependant(obs: RemoteReactiveDependant[A]) {
    dependencies -= obs
  }
  protected def prepareDependants(event: Transaction, commitVotes: Iterable[CommitVote], value: A) {
    val fork = if (commitVotes.size == 1) commitVotes.head else new ForkCommitVote(commitVotes)
    if (dependencies.size == 1) {
      dependencies.head.prepareCommit(event, fork, value);
    } else {
      val aggregator = new DelegateCountdownAggregateCommitVote(dependencies.size, fork);
      Reactive.executePooledForeach(dependencies) { _.prepareCommit(event, aggregator, value) };
    }
  }
  protected def commitDependants() {
    Reactive.executePooledForeach(dependencies) { _.commit }
  }
  protected def rollbackDependants() {
    Reactive.executePooledForeach(dependencies) { _.rollback }
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