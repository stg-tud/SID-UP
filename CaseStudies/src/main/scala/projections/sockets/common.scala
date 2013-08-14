package projections.sockets

import java.io._
import java.net._

package object common {
  def thread[F](f: => F) = {
    val t = new Thread( new Runnable() { def run() { f } } )
    t.start
    t
  }
}

import common._

trait Observer[I] {
  def receive(v: I): Unit
  def connect(port: Int) = thread {
    val ois = new ObjectInputStream(new Socket("127.0.0.1", port).getInputStream())
    while (true) receive(ois.readObject().asInstanceOf[I])
  }
}

trait Observable[I] {
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

case class Message[V](value: V, sender: String, direct: Boolean = false)
