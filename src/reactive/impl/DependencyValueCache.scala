package reactive.impl
import scala.collection.mutable
import reactive.Signal

class DependencyValueCache (from: DependencyValueCache) {
  private val values = mutable.Map[Signal[_], Any]();
  if (from != null) values ++= from.values
  def set[A](dependency: Signal[A], value: A) {
    values += (dependency -> value);
  }
  def get[A](dependency: Signal[A]) = values(dependency).asInstanceOf[A]
}

object DependencyValueCache {
  def initializeInstance(dependencies: Signal[_]*) = {
    val cache = new DependencyValueCache(null);
    def setFromNow[A](dependency: Signal[A]) {
      cache.set(dependency, dependency.now);
    }
    dependencies.foreach { setFromNow(_) }
    cache;
  }
}
