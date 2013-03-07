package reactive
package impl

import Reactive._
import util.Multiset
import remote.RemoteReactive
import remote.RemoteReactiveDependant

trait RemoteReactiveDependantImpl[-A] extends RemoteReactiveDependant[A] {
  self: ReactiveImpl[_] =>
  protected def connect(t: Txn, dependency: RemoteReactive[A]) {
    notify(dependency.addDependant(this)(t), None)(t);
  }
  protected def disconnect(t: Txn, dependency: RemoteReactive[A]) {
    notify(dependency.addDependant(this)(t), None)(t);
  }
}