package reactive
package impl

import java.util.UUID
import util.Update

trait ReactiveSourceImpl[A] extends ReactiveSource[A] {
  self : Reactive[_, _, _] =>
  override val uuid = UUID.randomUUID();
  override val sourceDependencies = Set(uuid)
  protected val noDependencyChange = new Update(sourceDependencies, sourceDependencies, false)
  override def isConnectedTo(transaction : Transaction) = transaction.sources.contains(uuid)
  private lazy val transaction = new TransactionBuilder();
  override def << (value: A) {
    transaction.set(this, value);
    transaction.commit();
  }
}