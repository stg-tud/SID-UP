package paperversion

import reactive.signals.Var
import reactive.Lift.single._
import reactive.ReactiveSource
import reactive.signals.Signal
import reactive.TransactionBuilder
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException
import reactive.Reactive

object Philosophers extends App {
  val size = 3

  // ============================================= Infrastructure ========================================================

  sealed trait Philosopher
  case object Thinking extends Philosopher
  case object Eating extends Philosopher

  sealed trait Fork
  case object Free extends Fork
  case object Occupied extends Fork
  case object DoubleUsageError extends Fork

  val calcFork = { (leftState: Philosopher, rightState: Philosopher) =>
    if (leftState == Eating && rightState == Eating) {
      DoubleUsageError
    } else if (leftState == Eating || rightState == Eating) {
      Occupied
    } else {
      Free
    }
  }
  val calcReady = { (leftState: Fork, rightState: Fork) =>
    leftState == Free && rightState == Free
  }

  // ============================================ Entity Creation =========================================================

  case class Seating(placeNumber: Integer, philosopher: Var[Philosopher], leftFork: Signal[Fork], rightFork: Signal[Fork], canEat: Signal[Boolean])
  def createTable(tableSize: Int): Seq[Seating] = {
    val phils = for (i <- 0 until tableSize) yield {
      Var[Philosopher](Thinking)
    }
    val forks = for (i <- 0 until tableSize) yield {
      calcFork(phils(i), phils((i + 1) % tableSize))
    }
    val canEat = for (i <- 0 until tableSize) yield {
      calcReady(forks(i), forks((i - 1 + tableSize) % tableSize))
    }
    for (i <- 0 until tableSize) yield {
      Seating(i, phils(i), forks(i), forks((i - 1 + tableSize) % tableSize), canEat(i))
    }
  }

  val seatings = createTable(size)
  val phils = seatings.map { _.philosopher }

  // ============================================== Logging =======================================================

  def log(msg: String): Unit = {
    println("[" + Thread.currentThread().getName() + " @ " + System.currentTimeMillis() + "] " + msg)
  }
  def log(reactive: Reactive[_, _]): Unit = {
    reactive.single.observe { value =>
      log(reactive + " now " + value)
    }
  }

  seatings.foreach { seating =>
    log(seating.philosopher)
    log(seating.leftFork)
    // right fork is the next guy's left fork
  }

  // ============================================ Runtime Behavior  =========================================================

  phils.foreach { philosopher =>
    philosopher.single.observe { state =>
      if (state == Eating)
        Future {
          philosopher << Thinking
        }
    }
  }

  @annotation.tailrec // unrolled into loop by compiler
  def repeatUntilTrue(op: => Boolean): Unit = if (!op) repeatUntilTrue(op)

  def eatOnce(seating: Seating) = {
    repeatUntilTrue {
      DependentUpdate(seating.canEat) { (admissions, canEat) =>
        if (canEat) {
          admissions += seating.philosopher -> Eating
          true // Don't try again
        } else {
          false // Try again
        }
      }
    }
  }

  // ============================================== Thread management =======================================================

  // ===================== STARTUP =====================
  // start simulation
  @volatile private var killed = false
  log("Starting simulation. Press <Enter> to terminate!")
  val threads = seatings.map { seating =>
    val phil = seating.philosopher
    phil ->
      Future {
        log("Controlling hunger on " + seating)
        while (!killed) {
          eatOnce(seating)
        }
        log(phil + " dies.")
      }
  }

  // ===================== SHUTDOWN =====================
  // wait for keyboard input
  System.in.read()

  // kill all philosophers
  log("Received Termination Signal, Terminating...")
  killed = true

  // collect forked threads to check termination
  threads.foreach {
    case (phil, thread) => try {
      import scala.language.postfixOps
      Await.ready(thread, 50 millis)
      log(phil + " terminated.")
    } catch {
      case te: TimeoutException => log(phil + " failed to terminate!")
    }
  }
}