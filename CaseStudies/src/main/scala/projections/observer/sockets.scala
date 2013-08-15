package projections.observer.sockets

import java.io._
import java.net._
import projections.observer.Message

package object common {
  def thread[F](f: => F) = {
    val t = new Thread( new Runnable() { def run() { f } } )
    t.start
    t
  }
}

import common._

trait Observer[I] extends projections.observer.Observer[I] {
  def connect(port: Int) = thread {
    val ois = new ObjectInputStream(new Socket("127.0.0.1", port).getInputStream())
    while (true) receive(ois.readObject().asInstanceOf[I])
  }
}

trait Observable[I] extends projections.observer.Observable[I] {
  val port: Int
  var observers = List[ObjectOutputStream]()
  def notifyObservers(v: I) = observers.foreach(_.writeObject(v))

  def startObservable() = thread {
    val serverSocket = new ServerSocket(port)
    while (true) {
      val sock = serverSocket.accept()
      observers ::= new ObjectOutputStream(sock.getOutputStream())
    }
  }
}

class Client extends projections.observer.Client with Observable[Seq[Int]] {
  val port = 27800
  override def init() = startObservable()
}

trait Division extends Observable[Message[Int]] with Observer[Seq[Int]] {
  this: projections.observer.Division =>
  override def init(): Unit = {
    connect(27800)
    startObservable()
  }
}

class Purchases(var perOrderCost: Int) extends projections.observer.Purchases with Division {
  val port = 27801
}

class Sales(val sleep: Int = 0) extends projections.observer.Sales with Division {
  val port = 27802
}

class Management extends projections.observer.Management with Observer[Message[Int]] with Observable[Int] {
  val port = 27803
  def init(): Unit = {
    connect(27801)
    connect(27802)
    startObservable()
  }
}
