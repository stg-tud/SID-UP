package reactive.impl

import scala.concurrent.stm._
import reactive.{Transaction, Reactive}
import util.ParallelForeach


trait DependencyImpl {
  this: ReactiveImpl[_, _] =>

  private[reactive] val dependants: TSet[Reactive.Dependant] = TSet[Reactive.Dependant]()

  override def addDependant(transaction: Transaction, dependant: Reactive.Dependant): Unit = atomic { implicit tx =>
    logger.trace(s"$dependant <~ $this [${Option(transaction).map { _.uuid } }]")
    dependants += dependant
  }

  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant): Unit = atomic { implicit tx =>
    logger.trace(s"$dependant <!~ $this [${Option(transaction).map { _.uuid } }]")
    dependants -= dependant
  }

  protected[reactive] def pingDependants(transaction: Transaction, sourceDependenciesChanged: Boolean, hasPulsed: Boolean): Unit = {
    ParallelForeach.parallelForeach(dependants.snapshot) { _.ping(transaction, sourceDependenciesChanged, hasPulsed) }
  }
}
