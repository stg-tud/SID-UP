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

abstract class ReactiveImpl[A](val name: String) extends Reactive[A] {
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

  private val ordering = new EventOrderingCache[Option[A]](sourceDependencies) {
    override def eventReadyInOrder(event: Event, value: Option[A]) {
      value match {
        case Some(newValue) =>
          observersLock.readLock().lock();
          observers.foreach { _(newValue) }
          observersLock.readLock().unlock();
        case None =>
      }
    }
  }
  protected def notifyDependants(event: Event) {
    dependenciesLock.readLock().lock();
    Reactive.executePooled(dependencies, { x: ReactiveDependant[_ >: A] =>
      x.notifyEvent(event)
    });
    dependenciesLock.readLock().unlock();
    ordering.eventReady(event, None);
  }

  protected def notifyDependants(event: Event, newValue: A) {
    dependenciesLock.readLock().lock();
    Reactive.executePooled(dependencies, { x: ReactiveDependant[_ >: A] =>
      x.notifyUpdate(event, newValue)
    });
    dependenciesLock.readLock().unlock();
    ordering.eventReady(event, Some(newValue));
  }

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