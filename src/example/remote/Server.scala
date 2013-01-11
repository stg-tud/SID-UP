package example.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import remote.RemoteReactive
import remote.RemoteDependant
import example.ResourceAllocationExample
import java.rmi.Naming
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry
import remote.EstablishConnectionData
import remote.RemoteReactive._

object Server extends App {
  Reactive.setThreadPoolSize(2);
  @remote trait RemoteServer {
    def connectToServer(requests: EstablishConnectionData[Int]): EstablishConnectionData[Int];
  }
  class RemoteServerImpl extends UnicastRemoteObject with RemoteServer {
    def connectToServer(requests: EstablishConnectionData[Int]): EstablishConnectionData[Int] = {
      send(ResourceAllocationExample.makeServer(receive(requests)))
    }
  }
  
  LocateRegistry.createRegistry(Registry.REGISTRY_PORT)
  Naming.rebind("remoteServer", new RemoteServerImpl);
  println("Server ready, awaiting client connections..");
}
