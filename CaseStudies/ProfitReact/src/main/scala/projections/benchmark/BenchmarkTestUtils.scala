package projections.benchmark

import java.util.concurrent.Semaphore

import com.typesafe.scalalogging.LazyLogging
import projections._
import reactive.remote.RemoteReactives
import reactive.signals.Var

trait InitReactives extends TestCommon {
  import projections.reactives._

  def name = "reactive"

  val registry = java.rmi.registry.LocateRegistry.createRegistry(1099)

  val setOrders = Var[Seq[Order]](List())
  val c = new Client(setOrders)
  val s = new Sales(0)
  val p = new Purchases(Var(perOrderCost))
  val m = new Management()

  RemoteReactives.lookupSignal[Int](projections.management).single.observe { v => done(v) }

  println("done")

  override def deinit() = {
    println(s"deinit $name")
    registry.list().foreach { name => println(s"unbind $name"); registry.unbind(name) }
    java.rmi.server.UnicastRemoteObject.unexportObject(registry, true)
    println("done")
  }
}

trait InitRMI extends TestCommon {
  import projections.observer._

  def name = "rmi"

  val registry = java.rmi.registry.LocateRegistry.createRegistry(1099)

  val c = new Client()
  val s = new Sales(0)
  val p = new Purchases(perOrderCost)
  val m = new Management()

  val managementObserver = new Observer[Int](projections.management) {
    def receive(v: Int) = done(v)
  }

  println("done")

  override def deinit() = {
    println(s"deinit $name")
    registry.list().foreach { name => println(s"unbind $name"); registry.unbind(name) }
    java.rmi.server.UnicastRemoteObject.unexportObject(registry, true)
    println("done")
  }
}

trait TestCommon extends LazyLogging {
  val sem = new Semaphore(0)

  def name: String

  var result: Int = 0

  val perOrderCost = 5

  override def toString = name

  println(s"initialize object $name")

  def apply(orders: Seq[Order]) = {
    // logger.info(s"test $name ${orders.size}")
    test(orders)
    // logger.info(s"await result")
    sem.acquire()
    // logger.info(s"result is ${result}")
    assert(orders.map { _.value }.sum - perOrderCost * orders.size == result)
  }

  def done(res: Int) = {
    result = res
    sem.release()
  }

  def test(v: Seq[Order]): Unit
  def deinit(): Unit = ()
}
