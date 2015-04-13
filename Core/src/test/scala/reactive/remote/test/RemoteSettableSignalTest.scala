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
import reactive.signals.SettableSignal
import reactive.events.EventStream

class RemoteSettableSignalTest extends FunSuite {
  try {
    val registry = java.rmi.registry.LocateRegistry.createRegistry(1099)

    test("basic signal behavior") {
      @remote trait MyServer {
        def double(in: Signal[Int]): Signal[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        def double(in: Signal[Int]): Signal[Int] = in.map(_ * 2)
      }
      Naming.rebind("signalServer", myServerImpl)
      try {

        val remoteServer = Naming.lookup("signalServer").asInstanceOf[MyServer]
        val request = SettableSignal[Int](5)
        val setter = EventSource[Int]
        request <<+ setter

        val response = remoteServer.double(request)
        assertResult(10) { response.now }

        setter << 7
        assertResult(14) { response.now }

        request <<- setter
        setter << 6
        assertResult(14) { response.now }
      } finally {
        Naming.unbind("signalServer")
      }
    }

    test("settable signal send") {
      @remote trait MyServer {
        def receive(settable: SettableSignal[Int]): Unit
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        var set: Option[SettableSignal[Int]] = None
        def receive(settable: SettableSignal[Int]): Unit = {
          set = Some(settable)
        }
      }
      Naming.rebind("settableReceiver", myServerImpl)
      try {
        val remoteServer = Naming.lookup("settableReceiver").asInstanceOf[MyServer]

        val clientSettable = SettableSignal[Int](5)
        val clientSetter = EventSource[Int]
        clientSettable <<+ clientSetter

        assert(myServerImpl.set.isEmpty)
        remoteServer.receive(clientSettable)
        assert(myServerImpl.set.isDefined)
        val serverSettable = myServerImpl.set.get
        assertResult(5) { serverSettable.now }

        clientSetter << 7
        assertResult(7) { serverSettable.now }

        val serverSetter = EventSource[Int]
        serverSettable <<+ serverSetter
        serverSetter << 6
        assertResult(6) { clientSettable.now }
        assertResult(6) { serverSettable.now }

        clientSettable <<- clientSetter
        clientSetter << 123
        assertResult(6) { clientSettable.now }
        assertResult(6) { serverSettable.now }

        serverSettable <<- serverSetter
        serverSetter << 234
        assertResult(6) { clientSettable.now }
        assertResult(6) { serverSettable.now }
      } finally {
        Naming.unbind("settableReceiver")
      }
    }

    test("settable signal receive") {
      @remote trait MyServer {
        def get(): SettableSignal[Int]
      }

      object myServerImpl extends UnicastRemoteObject with MyServer {
        val settable = SettableSignal[Int](5)
        def get: SettableSignal[Int] = settable
      }
      Naming.rebind("settableSender", myServerImpl)
      try {
        val remoteServer = Naming.lookup("settableSender").asInstanceOf[MyServer]

        val serverSettable = myServerImpl.settable
        val serverSetter = EventSource[Int]
        serverSettable <<+ serverSetter

        val clientSettable = remoteServer.get()

        assertResult(5) { clientSettable.now }

        serverSetter << 6
        assertResult(6) { clientSettable.now }
        assertResult(6) { serverSettable.now }

        val clientSetter = EventSource[Int]
        clientSettable <<+ clientSetter

        clientSetter << 7
        assertResult(7) { serverSettable.now }

        serverSettable <<- serverSetter
        serverSetter << 234
        assertResult(7) { clientSettable.now }
        assertResult(7) { serverSettable.now }

        clientSettable <<- clientSetter
        clientSetter << 123
        assertResult(7) { clientSettable.now }
        assertResult(7) { serverSettable.now }
      } finally {
        Naming.unbind("settableSender")
      }
    }

  } catch {
    case _: Exception =>
      println("registry already initialised")
  }
}
