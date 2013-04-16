package util

import reactive.Transaction

class TransactionalTransientVariable[A] {
  private var currentTransaction: Transaction = null
  private var value: A = _
  def set(transaction: Transaction, value: A) = {
    // TODO register "discard when transaction is done"
    // checkTransaction(transaction)
    currentTransaction = transaction
    this.value = value;
  }

  def get(transaction: Transaction) = {
    checkTransaction(transaction)
    value
  }

  def getIfSet(transaction: Transaction) = {
    if (currentTransaction == null) {
      None
    } else {
      Some(get(transaction))
    }
  }

  private def checkTransaction(transaction: Transaction) {
    if (!transaction.equals(transaction)) throw new IllegalStateException("Wrong transaction")
  }
}