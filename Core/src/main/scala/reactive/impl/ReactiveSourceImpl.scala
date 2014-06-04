package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.InTxn

trait ReactiveSourceImpl[A, P] extends ReactiveSource[A] {
  self: ReactiveImpl[_, P] =>
  override val uuid = UUID.randomUUID()
  protected val uuidSet = Set(uuid)
  override val name = s"ReactiveSource($uuid)"
  override def sourceDependencies(tx: InTxn) = uuidSet 
  override def isConnectedTo(transaction: Transaction) = transaction.sources.contains(uuid)
  def <<(value: A) {
    set(value)
  }
  def set(value: A){
    val transaction = new TransactionBuilder
    transaction.set(this, value)
    transaction.commit()
  }
  protected[reactive] def emit(transaction: Transaction,value: A){
    doPulse(transaction, sourceDependenciesChanged = false , makePulse(transaction.stmTx, value))
  }
  protected def makePulse(tx: InTxn, value: A): Option[P]
}

object ReactiveSourceImpl {
  trait ViewImpl[A] extends Reactive.View[A] {
    protected val impl: ReactiveSourceImpl[A, _]
    override protected[reactive] def sourceDependencies = impl.uuidSet
  }
}
