package elmish
package impl

import java.util.UUID

trait ReactiveSourceImpl[A, P] extends ReactiveSource[A] {
  self: ReactiveImpl[_, _, P] =>
  override val uuid = UUID.randomUUID();
  override val name = s"ReactiveSource($uuid)"
  private lazy val transaction = new TransactionBuilder();
  override def <<(value: A) {
    transaction.set(this, value);
    transaction.commit();
  }
  protected[elmish] def emit(transaction: Transaction,value: A/*,replyChannels: elmishUtil.TransactionAction => Unit**/){
    doPulse(transaction, false, makePulse(value))
  }
  protected def makePulse(value: A): Option[P]
}
