package reactive.remote.test

import reactive.events.EventSource
import org.scalatest.FunSuite
import reactive.events.EventStream
import java.rmi.server.UnicastRemoteObject
import java.rmi.Naming
import reactive.signals.Signal
import reactive.signals.Var
import reactive.signals.Val
import reactive.events.NothingEventStream

class RemoteTest extends FunSuite {
  try {
    val registry = java.rmi.registry.LocateRegistry.createRegistry(1099)

    test("eventstream") {
      @remote trait MyServer {
        def double(in: EventStream[Int]): EventStream[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        def double(in: EventStream[Int]): EventStream[Int] = in.map(_ * 2)
      }
      Naming.rebind("eventServer", myServerImpl)
      try {

        val remoteServer = Naming.lookup("eventServer").asInstanceOf[MyServer]
        //    val remoteServer = myServerImpl
        val request = EventSource[Int]
        val response = remoteServer.double(request)
        val log = response.log

        request << 5
        assertResult(Seq(10)) { log.now }

        request << 7
        assertResult(Seq(10, 14)) { log.now }

      } finally {
        Naming.unbind("eventServer")
      }
    }

    test("signal") {
      @remote trait MyServer {
        def double(in: Signal[Int]): Signal[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        def double(in: Signal[Int]): Signal[Int] = in.map(_ * 2)
      }
      Naming.rebind("signalServer", myServerImpl)
      try {

        val remoteServer = Naming.lookup("signalServer").asInstanceOf[MyServer]
        //    val remoteServer = myServerImpl
        val request = Var[Int](5)
        val response = remoteServer.double(request)

        assertResult(10) { response.now }

        request << 10
        assertResult(20) { response.now }

      } finally {
        Naming.unbind("signalServer")
      }
    }

    test("val") {
      @remote trait MyServer {
        def double(in: Signal[Int]): Signal[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        def double(in: Signal[Int]): Signal[Int] = in.map(_ * 2)
      }
      Naming.rebind("valServer", myServerImpl)
      try {

        val remoteServer = Naming.lookup("valServer").asInstanceOf[MyServer]
        //    val remoteServer = myServerImpl
        val request = Val(5)
        val response = remoteServer.double(request)

        assertResult(10) { response.now }
        assertResult(true) { response.isInstanceOf[Val[_]] }

      } finally {
        Naming.unbind("valServer")
      }
    }

    test("nothingeventstream") {
      @remote trait MyServer {
        def double(in: EventStream[Int]): EventStream[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        def double(in: EventStream[Int]): EventStream[Int] = in.map(_ * 2)
      }
      Naming.rebind("nothingServer", myServerImpl)
      try {

        val remoteServer = Naming.lookup("nothingServer").asInstanceOf[MyServer]
        //    val remoteServer = myServerImpl
        val request = NothingEventStream
        val response = remoteServer.double(request)

        assertResult(request) { response }

      } finally {
        Naming.unbind("nothingServer")
      }
    }

  } catch {
    case _: Exception =>
      println("registry already initialised")
  }
}
