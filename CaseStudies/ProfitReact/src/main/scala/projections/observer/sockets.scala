package projections.observer.sockets

import java.io._
import java.net._
import projections.Order
import projections.observer._

trait MakesThreads {
  var threads = List[Thread]()
  def thread[F](f: => F) = {
    val t = new Thread(new Runnable() { def run() { f } })
    t.setDaemon(true)
    t.start()
    threads ::= t
    t
  }
  def stopThreads() = threads.foreach { _.interrupt() }
}

trait Observer[I] extends projections.observer.Observer[I] with MakesThreads {
  def connect(port: Int) = thread {
    val ois = new ObjectInputStream(new Socket("127.0.0.1", port).getInputStream())
    try while (true) receive(ois.readObject().asInstanceOf[I])
    catch {
      case e: EOFException => println(s"ok on deinit: $e") // probably ok, socket closed before thread was interrupted …
    }
  }
}

trait Observable[I] extends projections.observer.Observable[I] with MakesThreads {
  val port: Int
  var observers = List[ObjectOutputStream]()
  def notifyObservers(v: I) = {
    observers.foreach { sock =>
      sock.writeObject(v)
      sock.flush()
    }
  }

  lazy val serverSocket = new ServerSocket(port)
  def startObserver() = thread {
    try {
      while (true) {
        val sock = serverSocket.accept()
        observers ::= new ObjectOutputStream(sock.getOutputStream())
      }
    } catch {
      case e: SocketException => println(s"ok on deinit: ${e.getMessage()}") // probably ok, socket closed before thread was interrupted …
    }
  }

  def deinit() = {
    stopThreads()
    serverSocket.close()
    observers.foreach { _.close() }
  }
}

class Client extends projections.observer.Client with Observable[Seq[Order]] {
  val port = 27800
  def init() = startObserver()
}

trait Division extends Observable[Message[Int]] with Observer[Seq[Order]] {
  this: projections.observer.Division =>
  override def init() = {
    connect(27800)
    startObserver()
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
  def init() = {
    connect(27801)
    connect(27802)
    startObserver()
  }

}

