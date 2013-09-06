package reactive
package signals
package impl

import reactive.impl.DynamicDependentReactive
import reactive.impl.ReactiveImpl
import reactive.impl.DependentReactive

object FlattenSignal {
  def apply[R <: Reactive[_, _, _, R]](outer: Signal[R]) : R = ???
}