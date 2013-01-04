package reactive

import scala.collection.mutable.MutableList
import scala.concurrent.ops.spawn
import util.ToStringHelper._
import util.Util._
import java.util.UUID

abstract class Reactive[A](val name: String, private var currentValue: A) {
  protected[reactive] val dependencies: MutableList[DependantReactive[_]] = MutableList()
  def addDependant(obs: DependantReactive[_]) {
    dependencies += obs
  }
  def sourceDependencies: Iterable[UUID]
  //  protected[reactive] def level: Int;

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

  def value = currentValue

  protected[this] def updateValue(source: UUID, event: UUID, newValue: A) {
    val changed = !nullSafeEqual(currentValue, newValue);
    if (changed) {
      currentValue = newValue;
      observers.foreach { _.notify(newValue) }
    }
    notifyDependencies(source, event, changed)
  }
  protected[this] def notifyDependencies(source: UUID, event: UUID, changed: Boolean): Unit = {
    dependencies.foreach { x =>
      spawn {
        x.notifyUpdate(source, event, changed)
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
  implicit def autoSignalToValue[A](signal: Reactive[A]): A = signal.value
}