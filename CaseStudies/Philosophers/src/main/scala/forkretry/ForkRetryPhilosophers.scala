package forkretry

import reactive.signals.Var
import reactive.signals.Signal
import reactive.signals.TransposeSignal
import reactive.Lift.single._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent._
import scala.concurrent.stm._
import scala.concurrent.duration._
import scala.concurrent.Await
import java.util.concurrent.TimeoutException

object RetryFork {
  case class MultipleRequestsException(fork: RetryFork) extends RuntimeException("Multiple Requests for " + fork)
}
// ===================== FORK IMPLEMENTATION =====================
case class RetryFork(id: Int) {
  // input
  val in = Var[Set[Signal[Option[RetryPhilosopher]]]](Set())
  def addPhilosopher(phil: RetryPhilosopher)(implicit tx: InTxn) {
    in << in.now + phil.request
  }

  // intermediate
  val requestStates = atomic { new TransposeSignal(in, _) }
  val requests = requestStates.single.map(_.flatten)

  // output
  val owner = requests.single.tmap { (requests, tx) =>
    if (requests.size > 1) throw new RetryFork.MultipleRequestsException(this)
    requests.headOption
  }
  val isOccupied = owner.single.map(_.isDefined)
}

// ===================== PHILOSOPHER IMPLEMENTATION =====================
object RetryPhilosopher {
  sealed trait State
  case object Thinking extends State
  case object Eating extends State
}

case class RetryPhilosopher(id: Int) {
  import RetryPhilosopher._

  // input
  val state: Var[State] = Var(Thinking)

  // auto-release forks whenever eating successfully
  state.single.changes.single.filter(_ == Eating).single.observe { _ => state << Thinking }

  // intermediate
  val request = state.single.map {
    case Thinking => None
    case Eating => Some(this)
  }

  // connect input to forks
  val forks = Var(Set[RetryFork]())
  def addFork(fork: RetryFork)(implicit tx: InTxn) = {
    fork.addPhilosopher(this)
    forks << forks.now + fork
  }

  // behavior
  def eatOnce() = {
	// Variant 1: try new transaction until one succeeds
    while (try {
      state << Eating
      false
    } catch {
      case e: RetryFork.MultipleRequestsException => false
    }) {}

	// Variant 2: rollback update transaction until it succeeds
//    atomic { tx =>
//      try {
//        state << Eating;
//      } catch {
//        case e: RetryFork.MultipleRequestsException => retry(tx)
//      }
//    }
  }
}

object RetryPhilosophers extends App {
  // ===================== PARAMETER PARSING =====================
  val sizeOfTable = if (args.length < 1) {
    println("Using default table size 3. Supply an integer number as first program argument to customize.")
    3
  } else {
    Integer.parseInt(args(0))
  }

  // ===================== TABLE SETUP =====================
  println("Setting up table with " + sizeOfTable + " forks and philosophers.")

  // create forks
  val fork = for (i <- 0 until sizeOfTable) yield new RetryFork(i)
  // create and run philosophers
  val philosopher = for (i <- 0 until sizeOfTable) yield new RetryPhilosopher(i)
  // connect philosophers with forks
  philosopher.foreach { phil =>
    atomic { implicit tx =>
      phil.addFork(fork(phil.id))
      phil.addFork(fork((phil.id + 1) % 3))
    }
  }

  // ===================== OBSERVATION SETUP =====================
  def log(msg: String) = {
    println("[" + Thread.currentThread().getName() + " @ " + System.currentTimeMillis() + "] " + msg)
  }

  // ---- fork state observation ----
  //  fork.foreach(_.owner.observe { owner =>
  //    log(this + " now " + (owner match {
  //      case Some(x) => "owned by " + x
  //      case None => "free"
  //    }))
  //  })

  // ---- philosopher state observation ----
  //  philosopher.foreach {
  //    _.state.observe { value =>
  //        log(this + " is now " + value);
  //      }
  //  }

  // ---- table state observation ----
  atomic { implicit tx =>
    val eatingStates = philosopher.map(_.request)
    val allEating = new TransposeSignal(eatingStates, tx).map(_.flatten)
    allEating.changes. /*filter(!_.isEmpty).*/ observe(eating => log("Now eating: " + eating))
  }

  // ===================== STARTUP =====================
  // start simulation
  @volatile private var killed = false
  println("Starting simulation. Press <Enter> to terminate!")
  val threads = philosopher.map { phil =>
    phil ->
      Future {
        Thread.currentThread().setName("p" + phil.id)
        println(phil + ": using " + phil.forks.single.now + " on thread" + Thread.currentThread().getName)
        while (!killed) {
          phil.eatOnce()
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
