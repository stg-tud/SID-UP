package reactive
import java.util.UUID

import scala.concurrent.stm.InTxn

case class Transaction(sources: scala.collection.Set[UUID], stmTx: InTxn)
