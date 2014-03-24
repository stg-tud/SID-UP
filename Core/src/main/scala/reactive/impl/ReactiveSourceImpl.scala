package reactive
package impl

import java.util.UUID
import scala.concurrent.stm.atomic

trait ReactiveSourceImpl[A, P] extends ReactiveSource[A] {
  self: ReactiveImpl[_, P] =>
  override val uuid = UUID.randomUUID()
  override val name = s"ReactiveSource($uuid)"

  override def sourceDependencies(transaction: Transaction) = Set(uuid)

  override def isConnectedTo(transaction: Transaction) = transaction.sources.contains(uuid)

  private lazy val transactionBuilder = new TransactionBuilder()

  override def <<(value: A) {
    transactionBuilder.set(this, value)
    transactionBuilder.commit()
  }

  protected[reactive] def emit(transaction: Transaction, value: A): Unit = {
    val pulse = makePulse(value)
    atomic { implicit tx =>
      addTransaction(transaction)
      setPulse(transaction, pulse)
    }
    pingDependants(transaction, sourceDependenciesChanged = false, hasPulsed = pulse.isDefined)
  }

  protected def makePulse(value: A): Option[P]
}
