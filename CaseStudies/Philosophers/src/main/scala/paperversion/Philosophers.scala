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

  sealed trait PhilosopherState
  case object Thinking extends PhilosopherState
  case object Eating extends PhilosopherState

  sealed trait ForkState
  case object Free extends ForkState
  case object Occupied extends ForkState

  val calcForkState = { (leftState: PhilosopherState, rightState: PhilosopherState) =>
    if (leftState == Eating && rightState == Eating) {
      throw new Exception("Fork already in use!")
    }

    if (leftState == Eating || rightState == Eating) {
      Occupied
    } else {
      Free
    }
  }

  // ============================================ Entity Creation =========================================================

  case class Seating(placeNumber: Integer, philosopher: Var[PhilosopherState], leftFork: Signal[ForkState], rightFork: Signal[ForkState])
  def createTable(tableSize: Int): Seq[Seating] = {
    val philosophers = 0 until tableSize map { _ => Var[PhilosopherState](Thinking) }
    val forks = 0 until tableSize map { i => calcForkState(philosophers(i), philosophers((i + 1) % tableSize)) }
    0 until tableSize map { i => Seating(i, philosophers(i), forks(i), forks((i - 1 + tableSize) % tableSize)) }
  }
  val seatings = createTable(size)

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

  seatings.foreach { seating =>
    val philosopher = seating.philosopher
    philosopher.single.observe { state =>
      if (state == Eating) Future { philosopher << Thinking }
    }
  }

  @annotation.tailrec // unrolled into loop by compiler
  def repeatUntilTrue(op: => Boolean): Unit = if (!op) repeatUntilTrue(op)

  def eatOnce(seating: Seating) = {
    repeatUntilTrue {
      ConditionalUpdate { attempt =>
        if (attempt.read(seating.leftFork) == Occupied) {
          false // Try again
        } else if (attempt.read(seating.rightFork) == Occupied) {
          false // Try again
        } else {
          attempt.admit(seating.philosopher, Eating)
          true // Don't try again
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