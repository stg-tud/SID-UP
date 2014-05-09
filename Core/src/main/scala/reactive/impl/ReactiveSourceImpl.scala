package reactive
package impl

import java.util.UUID
import reactive.Reactive.PulsedState
import scala.concurrent.stm.InTxn

trait ReactiveSourceImpl[A, P] extends ReactiveSource[A] {
  self: ReactiveImpl[_, P] =>
  override val uuid = UUID.randomUUID();
  protected val uuidSet = Set(uuid)
  override val name = s"ReactiveSource($uuid)"
  override def sourceDependencies(tx: InTxn) = uuidSet 
  override def isConnectedTo(transaction: Transaction) = transaction.sources.contains(uuid)
  private lazy val transaction = new TransactionBuilder();
  def <<(value: A) {
    set(value)
  }
  def set(value: A){
    transaction.set(this, value);
    transaction.commit();
  }
  def setOpen(value: A)(implicit tx: InTxn) {
    transaction.set(this, value);
    transaction.commitOpen()(tx);
  }
  protected[reactive] def emit(transaction: Transaction,value: A/*,replyChannels: util.TransactionAction => Unit**/){
    doPulse(transaction, false, makePulse(transaction.stmTx, value))
  }
  protected def makePulse(tx: InTxn, value: A): Option[P]
}

object ReactiveSourceImpl {
  trait ViewImpl[A] extends Reactive.View[A] {
    protected val impl: ReactiveSourceImpl[A, _]
    override protected[reactive] def sourceDependencies = impl.uuidSet
  }
}
