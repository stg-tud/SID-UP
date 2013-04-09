package reactive
package impl

import Reactive._
import remote.RemoteReactive
import remote.RemoteReactiveDependant
import dctm.vars.TransactionalVariable

trait RemoteReactiveDependantImpl[-A] extends RemoteReactiveDependant[A] {
  self: ReactiveImpl[_] =>
//  private TransactionalVariable[Map[dependency]]
  protected def connect(t: Txn, dependency: RemoteReactive[A]) {
    notify(dependency.addDependant(this)(t), None)(t);
  }
  protected def disconnect(t: Txn, dependency: RemoteReactive[A]) {
    notify(dependency.addDependant(this)(t), None)(t);
  }
}