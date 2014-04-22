package reactive

import java.util.UUID
import scala.concurrent.stm._
import com.typesafe.scalalogging.slf4j.StrictLogging
import scala.collection._
import scala.annotation.tailrec
import util.ParallelForeach

class Transaction(val sourceIDs: Set[UUID], val uuid: UUID = UUID.randomUUID()) extends StrictLogging {
  private val pulses: concurrent.Map[Reactive[_, _], Pulse[_]] = concurrent.TrieMap()
  private val needPing: concurrent.Map[Reactive.Dependant, Unit] = concurrent.TrieMap()
  private val awaitPulse: concurrent.Map[Reactive[_, _], Unit] = concurrent.TrieMap()

  protected[reactive] def pulse[O, P](reactive: Reactive[O, P]): Pulse[P] = {
    pulses(reactive).asInstanceOf[Pulse[P]]
  }

  protected[reactive] def hasPulsed(reactive: Reactive[_, _]): Boolean = {
    val res = pulses.contains(reactive)
    logger.trace(s"request pulse of reactive $reactive pulses is $pulses, result is $res in $this")
    res
  }

  protected[reactive] def setPulse[O, P](reactive: Reactive[O, P], pulse: Pulse[P]): Unit = {
    pulses += reactive -> pulse
    logger.trace(s"set pulse of $reactive to $pulse in $this")
    logger.trace(s"pulses is now $pulses in $this")
    awaitPulse -= reactive
  }

  protected[reactive] def pingDependants(dependants: Set[Reactive[_, _] with Reactive.Dependant]): Unit = {
    logger.trace(s"ping dependants $dependants")
    awaitPulse ++= dependants.map(_ -> (()))
    needPing ++= dependants.map(_ -> (()))
  }

  protected[reactive] def addAwait(reactives: Set[Reactive[_, _]]): Unit = awaitPulse ++= reactives.map(_ -> (()))

  protected[reactive] def addDependencies(transactions: Iterable[Transaction]): Unit = ()

  protected[reactive] def dependsOn(transaction: Transaction): Boolean = ???

  def commit() = atomic { implicit tx =>
    logger.trace(s"commit ${pulses.keys }")
    pulses.keys.foreach(_.commit(this))
  }

  @tailrec
  protected[reactive] final def propagate(): Unit = {
    logger.trace(s"propagate with pulses $pulses in $this")
    logger.trace(s"needPing: $needPing in $this")
    if (needPing.isEmpty) {
      logger.trace(s"awaitPulse: $awaitPulse in $this")
      logger.trace(s"pulses: $pulses in $this")
      if (awaitPulse.isEmpty) commit() else propagate()
    }
    else {
      val snapshot = needPing.keySet
      logger.trace(s"second propagate with pulses $pulses ping $needPing await $awaitPulse")
      if (!snapshot.isEmpty) {
        ParallelForeach.parallelForeach(snapshot) { dependency =>
          needPing -= dependency
          dependency.ping(this)
        }
      }
      propagate()
    }
  }

}
