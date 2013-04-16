package util

import reactive.Transaction

abstract class TransactionalAccumulator[A] {
  private var pendingTicks: Int = 0
  private var currentTransaction: Transaction = _
  private var value: A = _

  protected def expectedTickCount(transaction : Transaction) : Int
  protected def initialValue : A
  
  def tickAndGetIfCompleted[B](transaction: Transaction)(op: A => A) : Option[A] = {
    if (pendingTicks == 0) {
      currentTransaction = transaction
      pendingTicks = expectedTickCount(transaction)
      value = initialValue
    } else {
      checkTransaction(transaction)
    }
    value = op(value)
    pendingTicks -= 1
    if(pendingTicks == 0) {
      Some(value)
    } else {
      None
    }
  }
  
  def get(transaction : Transaction) = {
    checkTransaction(transaction)
    value
  }
  private def checkTransaction(transaction : Transaction) {
    if(!transaction.equals(currentTransaction)) throw new IllegalStateException("Wrong transaction!")
  }
}