package reactive.remote.test

import reactive.events.EventSource
import org.scalatest.FunSuite
import reactive.events.EventStream
import java.rmi.server.UnicastRemoteObject
import java.rmi.Naming
import reactive.signals.Signal
import reactive.signals.Var

class RemoteTest extends FunSuite {
  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }

  test("automatic remote eventstream works") {
    @remote trait MyServer {
      def double(in: EventStream[Int]): EventStream[Int]
    }

    object myServerImpl extends UnicastRemoteObject with MyServer {
      def double(in: EventStream[Int]): EventStream[Int] = in.map(_ * 2)
    }
    Naming.rebind("eventServer", myServerImpl)

    val remoteServer = Naming.lookup("eventServer").asInstanceOf[MyServer]
    //    val remoteServer = myServerImpl
    val request = EventSource[Int]
    val response = remoteServer.double(request)
    val log = response.log

    request << 5
    assertResult(Seq(10)) { log.now }

    request << 7
    assertResult(Seq(10, 14)) { log.now }
  }

  test("automatic remote signal works") {
    @remote trait MyServer {
      def double(in: Signal[Int]): Signal[Int]
    }

    object myServerImpl extends UnicastRemoteObject with MyServer {
      def double(in: Signal[Int]): Signal[Int] = in.map(_ * 2)
    }
    Naming.rebind("eventServer", myServerImpl)

    val remoteServer = Naming.lookup("eventServer").asInstanceOf[MyServer]
    //    val remoteServer = myServerImpl
    val request = Var[Int](5)
    val response = remoteServer.double(request)

    assertResult(10) { response.now }

    request << 10
    assertResult(20) { response.now }
  }

}
