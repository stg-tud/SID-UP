import reactive.signals.Var
import reactive.signals.Signal
import reactive.Lift._
import reactive.TransactionBuilder

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

object Philosophers extends App {
  val FREE = -1

  // a philosopher is eating whenever she owns both forks
  val calculateEating: (Int, Int, Int) => Boolean = (id, left, right) => id == left && id == right
  class Philosopher(val id: Int, val leftFork: Var[Int], val rightFork: Var[Int]) {
    // connect this philosopher's eating state with her forks
    val isEating = signal3(calculateEating)(id, leftFork, rightFork)
    // print a notification whenever this philosopher starts or stops eating
    isEating.observe { value =>
      println("Philosopher %d is %s eating.".format(id, if(value) "now" else "no longer"));
    }
  
    // kill-switch
    private var killed = false
    def kill() = killed = true
    // acting thread: The philosopher repeatedly tries to acquire both forks until she is killed
    Future {
       while(!killed) {
         // acquire forks
         val transaction = new TransactionBuilder
         transaction.cas(leftFork, FREE, id)
         transaction.cas(rightFork, FREE, id)
         transaction.commit();

         // release forks
         leftFork << FREE
         rightFork << FREE
       }
    }
  }

  
  // size of the table == number of forks == number of philosophers
  val n = if(args.length < 1) {
    println("Using default table size 3. Supply an integer as first argument to customize.");
    3
  } else { 
    Integer.parseInt(args(0))
  }

  // create forks
  val fork = for (i <- 0 until n) yield Var(FREE)
  // create and run philosophers
  val philosopher = for (i <- 0 until n) yield new Philosopher(i, fork((i - 1) % 3), fork((i + 1) % 3))

  // wait for user input
  System.in.read();
  // kill all philosophers
  philosopher.foreach(_.kill())
}