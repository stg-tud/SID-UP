package reactive.impl

import scala.concurrent.stm._
import reactive.{Transaction, Reactive}

//TODO: this will need to be refactored to use some kind of weak ref,
//TODO: such that reactive graphs can be garbage collected if downstream nodes are no longer needed
trait DependencyImpl {
  this: ReactiveImpl[_, _] =>

  private[reactive] val dependants: TSet[Reactive.Sink] = TSet[Reactive.Sink]()

  override def addDependant(transaction: Transaction, dependant: Reactive.Sink): Unit = atomic { implicit tx =>
    logger.trace(s"$dependant <~ $this [${Option(transaction).map { _.uuid } }]")
    dependants += dependant
  }

//  override def removeDependant(transaction: Transaction, dependant: Reactive.Dependant): Unit = atomic { implicit tx =>
//    logger.trace(s"$dependant <!~ $this [${Option(transaction).map { _.uuid } }]")
//    dependants -= dependant
//  }
//
//  protected[reactive] def pingDependants(transaction: Transaction): Unit = {
//    ParallelForeach.parallelForeach(dependants.snapshot) { _.ping(transaction) }
//  }
}
