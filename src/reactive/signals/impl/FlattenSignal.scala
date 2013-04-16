package reactive
package signals
package impl

import util.MutableValue

class FlattenSignal[A](val outer: Signal[Signal[A]]) extends {
  val inner = new MutableValue(outer.now)
} with SignalImpl[A](outer.sourceDependencies ++ inner.current.sourceDependencies, inner.current.now) {
  outer.addDependant(new Signal.Dependant[Signal[A]]{
    override def notify(notification : SignalNotification[Signal[A]]) { 
      
    }
  })
  val innerDependant = new Signal.Dependant[A] {
    override def notify(notification : SignalNotification[A]) {
      
    }
  }
  inner.current.addDependant(innerDependant)
  
}