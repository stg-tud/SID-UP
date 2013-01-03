package asdf

import scala.collection.mutable.MutableList
import util.ToStringHelper._
import util.Util._
import util.ThreadPool

abstract class Reactive[A](val name: String, private var currentValue: A) {
  protected[asdf] val dependencies: MutableList[Signal[_]] = MutableList()
  def addDependant(obs: Signal[_]) {
    dependencies += obs
  }

  def level: Int;

  private class ObserverHandler(name: String, op: A => Unit) {
    def notify(value: A) = op(value)
    override def toString = name
  }
  private val observers: MutableList[ObserverHandler] = MutableList()
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

  def sourceDependencies: Iterable[Var[_]]

  def value = currentValue

  protected def updateValue(pool: ThreadPool, source: Var[_], newValue: A) {
    val changed = !nullSafeEqual(currentValue, newValue);
    if (changed) {
      currentValue = newValue;
      observers.foreach { _.notify(newValue) }
    }
    notifyDependencies(pool, source, changed)
  }
  protected def notifyDependencies(pool: ThreadPool, source: asdf.Var[_], changed: Boolean): Unit = {
    dependencies.foreach { x =>
      pool.execute {
        x.notifyUpdate(pool, source, changed)
      }
    }
  }

  override def toString = name;
  def toElaborateString: String = {
    return toString(new StringBuilder(), 0, new java.util.HashSet[Reactive[_]]).toString;
  }
  def toString(builder: StringBuilder, depth: Int, done: java.util.Set[Reactive[_]]): StringBuilder = {
    indent(builder, depth).append("<").append(getClass().getSimpleName().toLowerCase());
    if (done.add(this)) {
      builder.append(" name=\"").append(name).append("\" level=\"").append(level).append("\">\n");
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
  implicit def autoSignalToValue[A](signal: Reactive[A]): A = signal.value
}