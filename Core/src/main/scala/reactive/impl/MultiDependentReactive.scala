package reactive
package impl

import java.util.UUID
import com.typesafe.scalalogging.slf4j.Logging

trait MultiDependentReactive extends Logging {
  self: Reactive[_, _] with DependentReactive[_] =>

  protected def dependencies: Set[Reactive[_, _]]

  private val _dependencies: Set[Reactive[_, _]] = dependencies

  _dependencies.foreach { _.addDependant(null, this) }

  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = _dependencies

  override protected def calculateSourceDependencies(transaction: Transaction): Set[UUID] = {
    _dependencies.flatMap(_.sourceDependencies(transaction))
  }
}
