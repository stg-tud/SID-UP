import reactive.signals.Var
import reactive.signals.Signal
import reactive.Lift._
import reactive.TransactionBuilder
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import reactive.signals.TransposeSignal
import scala.concurrent.stm.atomic
import scala.concurrent.stm.Txn

object Philosophers extends App {
  class Fork(val id: Int) {
    val in = Var[Set[Signal[Option[Int]]]](Set())
    private val requestStates = new TransposeSignal(in)
    private val requests = requestStates.map(_.flatten)
    requests.changes.filter(_.size > 1).observe(bla => println("Multiple owners requested: " + bla))
    val owner = requests.map(_.headOption)
    owner.observe { owner =>
      println("Fork " + id + " now " + (owner match {
        case Some(x) => "owned by " + x
        case None => "free"
      }))
    }
  }

  // a philosopher is eating whenever she owns both forks
  val calculateEating: (Int, Option[Int], Option[Int]) => Boolean = (id, left, right) => (left, right) match {
    case (Some(`id`), Some(`id`)) => true
    case _ => false
  }

  class Philosopher(val id: Int, val leftFork: Fork, val rightFork: Fork) {
    val tryingToEat = Var(false)
    val request = tryingToEat.map(_ match {
      case false => None
      case true => Some(id)
    })

    leftFork.in << leftFork.in.now + request
    rightFork.in << rightFork.in.now + request

    // connect this philosopher's eating state with her forks
    val isEating = signal3(calculateEating)(id, leftFork.owner, rightFork.owner)
    // print a notification whenever this philosopher starts or stops eating
    isEating.observe { value =>
      println("Philosopher %d is %s eating.".format(id, if (value) "now" else "no longer"));
    }

    // kill-switch
    private var killed = false
    def kill() = killed = true
    // acting thread: The philosopher repeatedly tries to acquire both forks until she is killed
    Future {
      println("Philosopher " + id + ": Thread id " + Thread.currentThread().getName())
      while (!killed) {
        atomic { tx =>
          if (leftFork.owner.now.isDefined) {
            println("Philosopher " + id + ": left fork " + leftFork.id + ": busy")
            Txn.retry(tx)
          } else {
            println("Philosopher " + id + ": left fork " + leftFork.id + ": free")
          }

          if (rightFork.owner.now.isDefined) {
            println("Philosopher " + id + ": right fork " + rightFork.id + ": busy")
            Txn.retry(tx)
          } else {
            println("Philosopher " + id + ": right fork " + rightFork.id + ": free")
          }

          tryingToEat << true
        }

        Thread.sleep(1000)
        // release forks
        tryingToEat << false
      }
    }
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
}