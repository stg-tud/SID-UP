package reactive
package impl

import java.util.UUID

trait ReactiveSourceImpl[A, P] extends ReactiveSource[A] {
  self: ReactiveImpl[_, _, P] =>
  override val uuid = UUID.randomUUID();
  override val name = s"ReactiveSource($uuid)"
  override def sourceDependencies(transaction: Transaction) = Set(uuid)
  override def isConnectedTo(transaction: Transaction) = transaction.sources.contains(uuid)
  private lazy val transaction = new TransactionBuilder();
  override def <<(value: A) {
    transaction.set(this, value);
    transaction.commit();
  }
  protected[reactive] def emit(transaction: Transaction,value: A/*,replyChannels: util.TransactionAction => Unit**/){
    doPulse(transaction, false, makePulse(value))
  }
  protected def makePulse(value: A): Option[P]
}
