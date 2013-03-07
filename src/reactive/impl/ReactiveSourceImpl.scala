package reactive
package impl

import Reactive._
import util.Multiset

trait ReactiveSourceImpl[A] extends ReactiveSource[A] {
  self : ReactiveImpl[A] =>
  def update(newValue: A)(implicit t : Txn) {
    notifyDependants(Multiset.empty, Some(newValue))
  }
}