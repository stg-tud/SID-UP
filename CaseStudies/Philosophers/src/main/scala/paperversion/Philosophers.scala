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

// ========================================================================================================

sealed trait PhilosopherState
object Thinking extends PhilosopherState
object Eating extends PhilosopherState

sealed trait ForkState
object Free extends ForkState
object Occupied extends ForkState

object log {
  def apply(msg: String) = {
    println("[" + Thread.currentThread().getName() + " @ " + System.currentTimeMillis() + "] " + msg)
  }
}

// ========================================================================================================

case class Philosopher(id: Int) {
  val state = Var[PhilosopherState](Thinking)

  state.single.changes.single.filter(_ == Eating).single.observe { _ =>
    log(this + " now eating.")
    state << Thinking
  }
}

object Fork {
  val calcState = { (leftState: PhilosopherState, rightState: PhilosopherState) =>
    if (leftState == Eating && rightState == Eating) {
      log("Error: Double Occupancy on " + this)
    }

    if (leftState == Eating || rightState == Eating) {
      Occupied
    } else {
      Free
    }
  }
}
case class Fork(leftPhilosopher: Philosopher, rightPhilosopher: Philosopher) {
  val state = Fork.calcState(leftPhilosopher.state, rightPhilosopher.state)
}

// ========================================================================================================

case class Seating(philosopher: Philosopher, leftFork: Fork, rightFork: Fork)
object Seating {
  def createTable(tableSize: Int): Seq[Seating] = {
    val philosophers = 0 until tableSize map { i => new Philosopher(i) }
    val forks = 0 until tableSize map { i => new Fork(philosophers(i), philosophers((i + 1) % tableSize)) }
    philosophers.map { philosopher => Seating(philosopher, forks(philosopher.id), forks((philosopher.id + 1) % tableSize)) }
  }
}

// ========================================================================================================

object Philosophers extends App {
  @annotation.tailrec def repeatUntilTrue (op: => Boolean): Unit = if(!op) repeatUntilTrue(op)
  
  def eatOnce(seating: Seating) = {
    repeatUntilTrue {
      ConditionalUpdate { attempt =>
        if (attempt.read(seating.leftFork.state) == Occupied) {
          false // Try again
        } else if (attempt.read(seating.rightFork.state) == Occupied) {
          false // Try again
        } else {
          attempt.admit(seating.philosopher.state, Eating)
          true // Don't try again
        }
      }
    }
  }

  val size = 3
  val seatings = Seating.createTable(size)

  // ===================== STARTUP =====================
  // start simulation
  @volatile private var killed = false
  log("Starting simulation. Press <Enter> to terminate!")
  val threads = seatings.map { seating =>
    val phil = seating.philosopher
    phil ->
      Future {
        Thread.currentThread().setName("Worker-" + phil.id)
        log(seating + " on thread" + Thread.currentThread().getName)
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