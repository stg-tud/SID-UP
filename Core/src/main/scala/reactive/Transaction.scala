package reactive

import java.util.UUID
import scala.concurrent.stm._

class Transaction(val sources: scala.collection.Set[UUID], val uuid: UUID = UUID.randomUUID()) {
  private val pulses = TMap[Reactive[_, _], Option[_]]()

  protected[reactive] def pulse[O, P](reactive: Reactive[O, P]): Option[P] = atomic { implicit tx =>
    pulses(reactive).asInstanceOf[Option[P]]
  }

  protected[reactive] def hasPulsed(reactive: Reactive[_, _]): Boolean = atomic { implicit tx =>
    pulses.contains(reactive)
  }

  protected[reactive] def setPulse[O, P](reactive: Reactive[O, P], pulse: Option[P]): Unit = atomic { implicit tx =>
    pulses += reactive -> pulse
  }

  protected[reactive] def addDependencies(transactions: Iterable[Transaction]): Unit = ()

  protected[reactive] def dependsOn(transaction: Transaction): Boolean = ???
}
