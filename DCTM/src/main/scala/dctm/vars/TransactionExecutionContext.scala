package dctm.vars

import dctm.locks.TransactionLock
import dctm.commit._

case class TransactionExecutionContext[T](tid : T, commitVote : CommittableRegistry[T])