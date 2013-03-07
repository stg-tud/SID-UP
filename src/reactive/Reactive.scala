package reactive

import remote.RemoteReactive
import dctm.vars.TransactionExecutionContext
import Reactive._

/**
 *  Note: while this class implements a remote interface, it doesn't actually
 *  provide remoting capabilities for performance reasons. The interface is
 *  implemented only to provide a remote-capable wrapper to just forward all
 *  method invocations.
 */
trait Reactive[+A] extends RemoteReactive[A] {
  val name: String;
  def log(implicit t: Txn = null): Signal[List[A]]
  def observe(obs: A => Unit)
  def unobserve(obs: A => Unit)
}

object Reactive {
  type Txn = TransactionExecutionContext[Transaction]
}