package asdf

import scala.collection.mutable.MutableList
import util.ToStringHelper._
import util.Util._

abstract class Reactive[A](val name: String, private var currentValue: A) {
  private val dependencies: MutableList[Signal[_]] = MutableList()
  def addObserver(obs: Signal[_]) {
    dependencies += obs
  }

  def level : Int;
  
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

  private var _dirty = false
  def isDirty = _dirty

  protected def dirty {
    if (!_dirty) {
      _dirty = true
      dependencies.foreach(_.notifyDirty())
    }
  }

  def value = currentValue
  protected def newValueAndClean(value: A) {
    if (!_dirty) throw new IllegalStateException("Must be dirty before setting a value!");
    _dirty = false;
    if (nullSafeEqual(currentValue, value)) {
      dependencies.foreach(_.notifyClean(false))
    } else {
      currentValue = value;
      observers.foreach(_.notify(value))
      dependencies.foreach(_.notifyClean(true))
    }
  }

  override def toString: String = {
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