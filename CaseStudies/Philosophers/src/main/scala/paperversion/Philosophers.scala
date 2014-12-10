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
import scala.util.Random

object Philosophers extends App {
  val names = Random.shuffle(List("Agripina", "Alberto", "Alverta", "Beverlee", "Bill", "Bobby", "Brandy", "Caleb", "Cami", "Candice", "Candra", "Carter", "Cassidy", "Corene", "Danae", "Darby", "Debi", "Derrick", "Douglas", "Dung", "Edith", "Eleonor", "Eleonore", "Elvera", "Ewa", "Felisa", "Fidel", "Filiberto", "Francesco", "Georgia", "Glayds", "Hal", "Jacque", "Jeff", "Joane", "Johnny", "Lai", "Leeanne", "Lenard", "Lita", "Marc", "Marcelina", "Margret", "Maryalice", "Michale", "Mike", "Noriko", "Pete", "Regenia", "Rico", "Roderick", "Roxie", "Salena", "Scottie", "Sherill", "Sid", "Steve", "Susie", "Tyrell", "Viola", "Wilhemina", "Zenobia"))

  val size = 3

  if (size >= names.size) throw new IllegalArgumentException("Not enough names!")

  // ============================================= Infrastructure ========================================================

  sealed trait Philosopher
  case class Thinking(name: String) extends Philosopher {
    override def toString() = name + " is thinking..."
  }
  case class Hungry(name: String, food: String) extends Philosopher {
    override def toString() = name + " is hungry for " + food
  }

  sealed trait Fork
  case object Free extends Fork {
    override def toString() = "Fork is free"
  }
  case class Occupied(name: String, food: String) extends Fork {
    override def toString() = "Fork holds " + food + " for " + name
  }

  sealed trait State
  case object Ready extends State {
    override def toString() = "Forks are available."
  }
  case class Eating(food: String) extends State {
    override def toString() = "Eating " + food
  }
  case class Blocked(name: String, food: String) extends State {
    override def toString() = "Waiting for neighbor " + name + " to finish eating " + food
  }

  def calcFork(philosophers: (Philosopher, Philosopher)): Fork =
    philosophers match {
      case (Thinking(_), Thinking(_)) => Free
      case (Hungry(name, food), _) => Occupied(name, food)
      case (_, Hungry(name, food)) => Occupied(name, food)
    }

  def calcState(forks: (Fork, Fork)): State =
    forks match {
      case (Free, Free) => Ready
      case (Occupied(leftName, leftFood), Occupied(rightName, rightFood))
        if (leftName == rightName && leftFood == rightFood) =>
          Eating(leftFood)
      case (Occupied(name, food), _) => Blocked(name, food)
      case (_, Occupied(name, food)) => Blocked(name, food)
    }

  // ============================================ Entity Creation =========================================================

  case class Seating(placeNumber: Integer, philosopher: Var[Philosopher], leftFork: Signal[Fork], rightFork: Signal[Fork], canEat: Signal[State])
  def createTable(tableSize: Int): Seq[Seating] = {
    val phils = for (i <- 0 until tableSize) yield {
      Var[Philosopher](Thinking(names(i)))
    }
    val forks = for (i <- 0 until tableSize) yield {
      Function.untupled(calcFork _)(phils(i), phils((i + 1) % tableSize))
    }
    val state = for (i <- 0 until tableSize) yield {
      Function.untupled(calcState _)(forks(i), forks((i - 1 + tableSize) % tableSize))
    }
    for (i <- 0 until tableSize) yield {
      Seating(i, phils(i), forks(i), forks((i - 1 + tableSize) % tableSize), state(i))
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
      log(reactive + " now: " + value)
    }
  }

  seatings.foreach { seating =>
    log(seating.philosopher)
    log(seating.leftFork)
    // right fork is the next guy's left fork
    log(seating.canEat)
  }

  // ============================================ Runtime Behavior  =========================================================

  phils.foreach { philosopher =>
    philosopher.single.observe { state =>
      state match {
        case Hungry(name, _) =>
          Future {
            philosopher << Thinking(name)
          }
        case _ => // ignore
      }
    }
  }

  @annotation.tailrec // unrolled into loop by compiler
  def repeatUntilTrue(op: => Boolean): Unit = if (!op) repeatUntilTrue(op)

  def eatOnce(seating: Seating) = {
    repeatUntilTrue {
      DependentUpdate(seating.canEat) {
        (writes, canEat) =>
          if (canEat == Ready) {
            writes += seating.philosopher -> Hungry(names(seating.placeNumber), "Broccoli")
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