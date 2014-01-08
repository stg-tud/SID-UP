package reactive
package signals
package impl

import reactive.impl.SingleDependentReactive

class MapSignal[A, B](override val dependency: Signal[B], op: B => A) extends DependentSignalImpl[A] with SingleDependentReactive[A, Reactive.IDENTITY, Reactive.IDENTITY, Reactive.IDENTITY, Signal] {
  protected def reevaluateValue(transaction: Transaction): A = op(dependency.value(transaction))
}
