package reactive
package impl

import com.typesafe.scalalogging.Logging

trait MultiDependentReactive {
  self: ReactiveImpl[_, _] with DependentReactive[_] =>

  protected def dependencies: Set[Reactive[_, _]]

  private val _dependencies: Set[Reactive[_, _]] = dependencies

  dependencies.foreach { _.addDependant(new Transaction(Set()), this) }

  override def dependencies(transaction: Transaction): Set[Reactive[_, _]] = dependencies

}
