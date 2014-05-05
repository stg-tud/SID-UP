import reactive.signals.Var
import reactive.signals.Signal
import reactive.Lift._
import reactive.TransactionBuilder
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import reactive.signals.TransposeSignal
import scala.concurrent.stm._
import scala.concurrent.duration._
import reactive.Reactive
import reactive.events.EventStream
import java.io.BufferedInputStream
import scala.concurrent.Await
import java.util.concurrent.TimeoutException

// ======= FORK IMPLEMENTATION =======

class Fork(val id: Int) {
  // input
  val in = Var[Set[Signal[Option[Philosopher]]]](Set())

  // intermediate
  val requestStates = new TransposeSignal(in)
  val requests = requestStates.map(_.flatten)

  // output
  val owner = requests.map(_.headOption)

  // infrastructure
  override def toString(): String = "Fork " + id
}

// ======= PHILOSOPHER IMPLEMENTATION =======

object Philosopher {
  // function: a philosopher is eating whenever she owns both forks
  val calculateEating: (Philosopher, Option[Philosopher], Option[Philosopher]) => Boolean = (id, left, right) => (left, right) match {
    case (Some(`id`), Some(`id`)) => true
    case _ => false
  }
}

class Philosopher(val id: Int, val leftFork: Fork, val rightFork: Fork, val eatingDuration: Int) {
  // input
  val tryingToEat = Var(false)

  // intermediate
  val request = tryingToEat.map(_ match {
    case false => None
    case true => Some(this)
  })

  // connect input to forks
  leftFork.in << leftFork.in.now + request
  rightFork.in << rightFork.in.now + request

  // connect output from forks
  val isEating = signal3(Philosopher.calculateEating)(this, leftFork.owner, rightFork.owner)

  // infrastructure
  override def toString(): String = "Philosopher " + id

  // behavior
  def eatOnce() = {
    atomic { tx =>
      // await free forks
      if (leftFork.owner.now.isDefined || rightFork.owner.now.isDefined) {
        retry(tx)
      }
      // try to take forks
      tryingToEat << true
    }

    // "eat"
    Thread.sleep(eatingDuration)

    // release forks
    tryingToEat << false
  }
}

object Philosophers extends App {
  // ======= PARAMETER PARSING =======
  val sizeOfTable = if (args.length < 1) {
    println("Using default table size 3. Supply an integer number as first program argument to customize.");
    3
  } else {
    Integer.parseInt(args(0))
  }

  val eatingDuration = 100

  // ======= TABLE SETUP =======
  println("Setting up table with " + sizeOfTable + " forks and philosophers")

  // create forks
  val fork = for (i <- 0 until sizeOfTable) yield new Fork(i)
  // create and run philosophers
  val philosopher = for (i <- 0 until sizeOfTable) yield new Philosopher(i, fork(i), fork((i + 1) % 3), eatingDuration)

  // ======= OBSERVATION SETUP =======
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
  val allEating = new TransposeSignal(philosopher.map(p => (p.isEating.map(_ -> p)))).map(_.filter(_._1).map(_._2))
  allEating.changes.filter(!_.isEmpty).observe(eating => log("Now eating: " + eating))

  // ======= RUN TIME HANDLING =======
  // start simulation
  private var killed = false
  println("Starting simulation. Hit <Enter> to terminate!")
  val threads = philosopher.map { phil =>
    phil ->
      Future {
        println(phil + ": using " + phil.leftFork + " and " + phil.rightFork + " on thread" + Thread.currentThread().getName())
        while (!killed) {
          phil.eatOnce
        }
        log(phil + " dies.")
      }
  }

  // wait for keyboard input
  System.in.read()

  // kill all philosophers
  println("Received Termination Signal, Terminating...")
  killed = true
  val maxCompletionTime = System.currentTimeMillis() + sizeOfTable * (eatingDuration + 10)

  // collect forked threads to check termination
  threads.foreach {
    case (phil, thread) => try {
      import scala.language.postfixOps
      Await.ready(thread, (maxCompletionTime - System.currentTimeMillis()) millis)
      println(phil + " terminated.")
    } catch {
      case te: TimeoutException => println(phil + " failed to terminate!")
    }
  }
}