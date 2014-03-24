package reactive
import java.util.UUID

class Transaction(val sources: scala.collection.Set[UUID], val uuid: UUID = UUID.randomUUID()) {
  def pulse[O, P](reactive: Reactive[O, P]): Option[P] = ???
  def setPulse[O, P](reactive: Reactive[O, P], pulse: P): Unit = ???
  def addDependencies(transactions: Iterable[Transaction]): Unit = ???
  def dependsOn(transaction: Transaction): Boolean = ???
}
