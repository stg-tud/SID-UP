package example.remote
import Server.RemoteServer
import java.rmi.Naming
import example.ResourceAllocationExample
import example.ResourceAllocationExample.ServerFactory
import reactive.Reactive
import remote.RemoteReactiveImpl
import remote.RemoteDependantReactive
import javax.swing.JOptionPane

object Client extends App {
  Reactive.setThreadPoolSize(2);
  val host = JOptionPane.showInputDialog(null, "Server host name?", "Client Startup", JOptionPane.QUESTION_MESSAGE);
  if (host != null) {
    val remote = Naming.lookup("//" + host + "/remoteServer").asInstanceOf[RemoteServer];
    ResourceAllocationExample.makeClient(new ServerFactory {
      def connectToServer(requests: Reactive[Int]) = {
        new RemoteDependantReactive(remote.connectToServer(new RemoteReactiveImpl(requests))).reactive
      }
    })
  }
}