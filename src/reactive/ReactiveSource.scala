package reactive

import java.util.UUID
import dctm.vars.TransactionExecutionContext
import Reactive._

trait ReactiveSource[-A] extends Reactive[A] {
  private val transaction = new TransactionBuilder();
  protected def emit(value: A) = {
    transaction.set(this, value);
    transaction.commit();
  }

  val uuid = UUID.randomUUID();
  override val sourceDependencies = Set(uuid)
  def update(implicit t : Txn, value: A)
}