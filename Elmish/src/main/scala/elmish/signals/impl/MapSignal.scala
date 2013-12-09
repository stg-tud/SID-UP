package elmish
package signals
package impl

import elmish.impl.SingleDependentReactive

class MapSignal[A, B](override val dependency: Signal[B], op: B => A) extends DependentSignalImpl[A] with SingleDependentReactive {
  protected def reevaluateValue(transaction: Transaction): A = op(dependency.value(transaction))
}
