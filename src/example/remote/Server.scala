package example.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import remote.RemoteEventStream
import example.ResourceAllocationExample
import java.rmi.Naming
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import remote.EstablishEventStreamConnectionData
import remote.RemoteSignal._
import remote.EstablishSignalConnectionData

object Server extends App {
  Reactive.setThreadPoolSize(2);
  @remote trait RemoteServer {
    def connectToServer(requests: EstablishSignalConnectionData[Int]): EstablishSignalConnectionData[Int];
  }
  class RemoteServerImpl extends UnicastRemoteObject with RemoteServer {
    def connectToServer(requests: EstablishSignalConnectionData[Int]): EstablishSignalConnectionData[Int] = {
      send(ResourceAllocationExample.makeServer(receive(requests)))
    }
  }
  
  LocateRegistry.createRegistry(Registry.REGISTRY_PORT)
  Naming.rebind("remoteServer", new RemoteServerImpl);
  println("Server ready, awaiting client connections..");
  while(true) {
    System.gc();
    Thread.sleep(666);
  }
}
