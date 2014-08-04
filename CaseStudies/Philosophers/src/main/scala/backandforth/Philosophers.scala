package backandforth

import java.util.concurrent.TimeoutException

import reactive.Lift.single._
import reactive.signals.{Signal, TransposeSignal, Var}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.stm._
import scala.language.postfixOps

// ===================== FORK IMPLEMENTATION =====================
case class Fork(id: Int) {
  // input
  val in = Var[Set[Signal[Option[Philosopher]]]](Set())
  def addPhilosopher(phil: Philosopher)(implicit tx: InTxn): Unit = {
    in << in.now + phil.request
  }

  // intermediate
  val requestStates = atomic { new TransposeSignal(in, _) }
  val requests = requestStates.single.map(_.flatten)

  // output
  val owner = requests.single.map(_.headOption)
  val isOccupied = owner.single.map(_.isDefined)
}

// ===================== PHILOSOPHER IMPLEMENTATION =====================
object Philosopher {
  // function: a philosopher is eating whenever she owns all connected forks
  val calculateEating: (Philosopher, Iterable[Option[Philosopher]]) => Boolean =
    (philosopher, forks) => forks.find(_ != Some(philosopher)).isEmpty
}

case class Philosopher(id: Int) {
  // input
  val tryingToEat = Var(false)

  // intermediate
  val request = tryingToEat.single.map {
    case false => None
    case true => Some(this)
  }

  // connect input to forks
  val forks = Var(Set[Fork]())
  def addFork(fork: Fork)(implicit tx: InTxn) = {
    fork.addPhilosopher(this)
    forks << forks.now + fork
  }

  // connect output from forks
  val owners = atomic { new TransposeSignal(forks.single.map(_.map(_.owner)), _) }
  val isEating = signal2(Philosopher.calculateEating)(this, owners)

  // behavior
  def eatOnce() = {
    atomic { tx =>
      // await free forks
      if (forks.now(tx).exists(_.isOccupied.now(tx))) {
        retry(tx)
      }
      Txn.afterRollback(_ => println(this + " suffered fork acquisition failure!"))(tx)

      // try to take forks
      tryingToEat << true
    }

    // release forks
    atomic { tx =>
      tryingToEat << false
    }
  }
}

object Philosophers extends App {
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
  val fork = for (i <- 0 until sizeOfTable) yield new Fork(i)
  // create and run philosophers
  val philosopher = for (i <- 0 until sizeOfTable) yield new Philosopher(i)
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
  //    _.isEating.observe { value =>
  //        log(this + " is " + (if (value) "now" else "no longer") + " eating");
  //      }
  //  }

  // ---- table state observation ----
  atomic { implicit tx =>
    val eatingStates = philosopher.map(p => p.isEating.map(_ -> p))
    val allEating = new TransposeSignal(eatingStates, tx).map(_.collect { case (true, phil) => phil })
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
