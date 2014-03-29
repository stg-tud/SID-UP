package reactive

import java.util.UUID
import scala.concurrent.stm._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.scalalogging.slf4j.Logging
import scala.annotation.tailrec

class Transaction(val sourceIDs: Set[UUID], val uuid: UUID = UUID.randomUUID()) extends Logging {
  private val pulses = TMap[Reactive[_, _], Pulse[_]]()
  private val needPing = TSet[Reactive.Dependant]()
  private val awaitPulse = TSet[Reactive[_, _]]()

  protected[reactive] def pulse[O, P](reactive: Reactive[O, P]): Pulse[P] = atomic { implicit tx =>
    pulses(reactive).asInstanceOf[Pulse[P]]
  }

  protected[reactive] def hasPulsed(reactive: Reactive[_, _]): Boolean = atomic { implicit tx =>
    val res = pulses.contains(reactive)
    logger.trace(s"request pulse of reactive $reactive pulses is $pulses, result is $res in $this")
    res
  }

  protected[reactive] def setPulse[O, P](reactive: Reactive[O, P], pulse: Pulse[P]): Unit = atomic { implicit tx =>
    pulses += reactive -> pulse
    logger.trace(s"set pulse of $reactive to $pulse in $this")
    logger.trace(s"pulses is now $pulses in $this")
    awaitPulse -= reactive
  }

  protected[reactive] def pingDependants(dependants: TSet[Reactive[_, _] with Reactive.Dependant])(implicit tx: InTxn): Unit = {
    logger.trace(s"ping dependants $dependants")
    needPing ++= dependants
    awaitPulse ++= dependants
  }

  protected[reactive] def addAwait(reactives: TSet[Reactive[_, _]])(implicit tx: InTxn): Unit = awaitPulse ++= reactives

  protected[reactive] def addDependencies(transactions: Iterable[Transaction]): Unit = ()

  protected[reactive] def dependsOn(transaction: Transaction): Boolean = ???

  def commit()(implicit tx: InTxn) = {
    logger.trace(s"commit ${pulses.keys}")
    pulses.keys.foreach(_.commit(this))
  }

  //@tailrec
  protected[reactive] final def propagate(): Unit = {
    logger.trace(s"propagate with pulses $pulses in $this")
    val toPing = atomic { implicit tx =>
      logger.trace(s"needPing: $needPing in $this")
      if (needPing.isEmpty) {
        logger.trace(s"awaitPulse: $awaitPulse in $this")
        logger.trace(s"pulses: $pulses in $this")
        if (awaitPulse.isEmpty) commit() else retry
        Set.empty
      }
      else {
        val snapshot = needPing.snapshot
        needPing.clear()
        snapshot
      }
    }
    //TODO: this … does not work as expected
    future {atomic { implicit tx =>
      logger.trace(s"second propagate with pulses $pulses ping $needPing await $awaitPulse")
    } }
    if (!toPing.isEmpty) {
      //future {
        toPing.foreach(_.ping(this))
      //}
      propagate()
    }
  }

}
