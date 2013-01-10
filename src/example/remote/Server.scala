package example.remote

import java.rmi.server.UnicastRemoteObject
import reactive.Reactive
import remote.RemoteReactive
import remote.RemoteDependant
import example.ResourceAllocationExample
import remote.RemoteDependantReactive
import remote.RemoteReactiveImpl
import java.rmi.Naming
import java.rmi.registry.LocateRegistry
import java.rmi.registry.Registry

object Server extends App {
  Reactive.setThreadPoolSize(2);
  @remote trait RemoteServer {
    def connectToServer(requests: RemoteReactive[Int]): RemoteReactive[Int];
  }
  class RemoteServerImpl extends UnicastRemoteObject with RemoteServer {
    def connectToServer(requests: RemoteReactive[Int]): RemoteReactive[Int] = {
      new RemoteReactiveImpl(ResourceAllocationExample.makeServer(new RemoteDependantReactive(requests).reactive))
    }
  }
  
  LocateRegistry.createRegistry(Registry.REGISTRY_PORT)
  Naming.rebind("remoteServer", new RemoteServerImpl);
  println("Server ready, awaiting client connections..");
}
