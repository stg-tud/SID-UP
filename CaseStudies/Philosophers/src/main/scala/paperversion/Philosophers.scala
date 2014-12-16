package paperversion

import reactive.signals.Var
import reactive.Lift
import reactive.Lift._
import reactive.ReactiveSource
import reactive.signals.Signal
import reactive.TransactionBuilder
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import java.util.concurrent.TimeoutException
import reactive.Reactive
import scala.util.Random
import reactive.signals.Val

object Philosophers extends App {
  val names = Random.shuffle(List("Agripina", "Alberto", "Alverta", "Beverlee", "Bill", "Bobby", "Brandy", "Caleb", "Cami", "Candice", "Candra", "Carter", "Cassidy", "Corene", "Danae", "Darby", "Debi", "Derrick", "Douglas", "Dung", "Edith", "Eleonor", "Eleonore", "Elvera", "Ewa", "Felisa", "Fidel", "Filiberto", "Francesco", "Georgia", "Glayds", "Hal", "Jacque", "Jeff", "Joane", "Johnny", "Lai", "Leeanne", "Lenard", "Lita", "Marc", "Marcelina", "Margret", "Maryalice", "Michale", "Mike", "Noriko", "Pete", "Regenia", "Rico", "Roderick", "Roxie", "Salena", "Scottie", "Sherill", "Sid", "Steve", "Susie", "Tyrell", "Viola", "Wilhemina", "Zenobia"))

  val size = 3

  if (size >= names.size) throw new IllegalArgumentException("Not enough names!")

  // ============================================= Infrastructure ========================================================

  sealed trait Philosopher
  case object Thinking extends Philosopher
  case object Hungry extends Philosopher

  sealed trait Fork
  case object Free extends Fork
  case class Taken(name: String) extends Fork

  sealed trait Vision
  case object Ready extends Vision
  case object Eating extends Vision
  case class WaitingFor(name: String) extends Vision

  def calcFork (leftName: String, leftState: Philosopher, rightName: String, rightState: Philosopher): Fork =
    (leftState, rightState) match {
      case (Thinking, Thinking) => Free
      case (Hungry, _) => Taken(leftName)
      case (_, Hungry) => Taken(rightName)
    }

  def calcVision(ownName: String, leftFork: Fork, rightFork: Fork): Vision =
    (leftFork, rightFork) match {
      case (Free, Free) => Ready
      case (Taken(`ownName`), Taken(`ownName`)) => Eating
      case (Taken(name), _) => WaitingFor(name)
      case (_, Taken(name)) => WaitingFor(name)
    }

  // ============================================ Entity Creation =========================================================

  case class Seating(placeNumber: Integer, philosopher: Var[Philosopher], leftFork: Signal[Fork], rightFork: Signal[Fork], vision: Signal[Vision])
  def createTable(tableSize: Int): Seq[Seating] = {
    val phils = for (i <- 0 until tableSize) yield {
      Var[Philosopher](Thinking)
        .withName(names(i))
    }
    val forks = for (i <- 0 until tableSize) yield {
      val nextCircularIndex = (i + 1) % tableSize
      Lift.signal4(calcFork)(names(i), phils(i), names(nextCircularIndex), phils(nextCircularIndex))
        .withName("Fork of " + names(i) + " and " + names(nextCircularIndex))
    }
    val state = for (i <- 0 until tableSize) yield {
      Lift.signal3(calcVision)(names(i), forks(i), forks((i - 1 + tableSize) % tableSize))
        .withName("Vision of " + names(i))
    }
    for (i <- 0 until tableSize) yield {
      Seating(i, phils(i), forks(i), forks((i - 1 + tableSize) % tableSize), state(i))
    }
  }

  val seatings = createTable(size)

  // ============================================== Logging =======================================================

  def log(msg: String): Unit = {
    println("[" + Thread.currentThread().getName() + " @ " + System.currentTimeMillis() + "] " + msg)
  }
  def log(reactive: Reactive[_, _]): Unit = {
    reactive.observe { value =>
      log(reactive + " now: " + value)
    }
  }

  seatings.foreach { seating =>
    log(seating.philosopher)
    log(seating.leftFork)
    // right fork is the next guy's left fork
    log(seating.vision)
  }

  // ============================================ Runtime Behavior  =========================================================

  seatings.foreach { seating =>
    seating.vision.observe { state =>
      state match {
        case Eating =>
          Future {
            seating.philosopher << Thinking
          }
        case _ => // ignore
      }
    }
  }

  @annotation.tailrec // unrolled into loop by compiler
  def repeatUntilTrue(op: => Boolean): Unit = if (!op) repeatUntilTrue(op)

  def eatOnce(seating: Seating) = {
    repeatUntilTrue {
      DependentUpdate(seating.philosopher) {
        rw =>
          if (rw.read(seating.vision) == Ready) {
            rw += seating.philosopher -> Hungry
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
        val originalName = Thread.currentThread().getName()
        try {
          Thread.currentThread().setName("Worker-" + names(seating.placeNumber))
          log("Controlling hunger on " + seating)
          while (!killed) {
            eatOnce(seating)
          }
          log(phil + " dies.")
        } finally {
          Thread.currentThread().setName(originalName)
        }
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