import reactive.signals.Var
import reactive.signals.Signal
import reactive.Lift._
import reactive.TransactionBuilder
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import reactive.signals.TransposeSignal
import scala.concurrent.stm.atomic
import scala.concurrent.stm.Txn
import reactive.Reactive
import reactive.events.EventStream

object Philosophers extends App {
  def log(msg: String) = {
    println("[" + Thread.currentThread().getName() + " @ " + System.currentTimeMillis() + "] " + msg)
  }
  def transientAndSteadyPrintObserve[X](x: Signal[X], observer: X => String) = {
    x.observe { value => log("[Steady] " + observer(value)) }
    x.map { value => log("[Transient] " + observer(value)) }
  }
  def transientAndSteadyPrintObserve[X](x: EventStream[X], observer: X => String) = {
    x.observe { value => log("[Steady] " + observer(value)) }
    x.map { value => log("[Transient] "+observer(value)) }
  }

  class Fork(val id: Int) {
    val in = Var[Set[Signal[Option[Philosopher]]]](Set())
    private val requestStates = new TransposeSignal(in)
    private val requests = requestStates.map(_.flatten)
    transientAndSteadyPrintObserve(requests.changes.filter(_.size > 1), "Multiple owners requested for " + this + ": " + _)
    val owner = requests.map(_.headOption)
    transientAndSteadyPrintObserve(owner, { owner: Option[Philosopher] =>
      this + " now " + (owner match {
        case Some(x) => "owned by " + x
        case None => "free"
      })
    })
    override def toString(): String = "Fork " + id
  }

  // a philosopher is eating whenever she owns both forks
  val calculateEating: (Philosopher, Option[Philosopher], Option[Philosopher]) => Boolean = (id, left, right) => (left, right) match {
    case (Some(`id`), Some(`id`)) => true
    case _ => false
  }

  class Philosopher(val id: Int, val leftFork: Fork, val rightFork: Fork) {
    val tryingToEat = Var(false)
    val request = tryingToEat.map(_ match {
      case false => None
      case true => Some(this)
    })

    leftFork.in << leftFork.in.now + request
    rightFork.in << rightFork.in.now + request

    // connect this philosopher's eating state with her forks
    val isEating = signal3(calculateEating)(this, leftFork.owner, rightFork.owner)
    // print a notification whenever this philosopher starts or stops eating
    isEating.observe { value =>
      log(this + " is " + (if (value) "now" else "no longer") + " eating");
    }

    // kill-switch
    private var killed = false
    def kill() = killed = true
    // acting thread: The philosopher repeatedly tries to acquire both forks until she is killed
    var working = true
    Future {
      println(this + ": Thread id " + Thread.currentThread().getName())
      while (!killed) {
        if (atomic { tx =>
          if (leftFork.owner.now.isEmpty) {
//            log(this + ": left " + leftFork + ": free")
            if (rightFork.owner.now.isEmpty) {
//              log(this + ": right " + rightFork + ": free")
              tryingToEat << true
              true
            } else {
//              log(this + ": right " + rightFork + ": busy")
              false
            }
          } else {
//            log(this + ": left " + leftFork + ": busy")
            false
          }
        }) {
          Thread.sleep(100)
          // release forks
          tryingToEat << false
        }
      }
      working = false
    }

    override def toString(): String = "Philosopher " + id
  }

  // size of the table == number of forks == number of philosophers
  val n = if (args.length < 1) {
    println("Using default table size 3. Supply an integer as first argument to customize.");
    3
  } else {
    Integer.parseInt(args(0))
  }

  // create forks
  val fork = for (i <- 0 until n) yield new Fork(i)
  // create and run philosophers
  val philosopher = for (i <- 0 until n) yield new Philosopher(i, fork(i), fork((i + 1) % 3))

  // wait for user input
  System.in.read();
  // kill all philosophers
  philosopher.foreach(_.kill())

  println("Received Termination Signal, Terminating...")
  Thread.sleep((n + 1) * 1000)
  println("Philosophers that have not terminated: " + philosopher.filter(_.working))
}