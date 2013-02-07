package example.remote
import Server.RemoteServer
import java.rmi.Naming
import example.ResourceAllocationExample
import example.ResourceAllocationExample.ServerFactory
import reactive.Reactive
import javax.swing.JOptionPane
import remote.RemoteSignal._
import java.awt.event.WindowListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import reactive.Signal

object Client extends App {
  Reactive.setThreadPoolSize(2);
  val host = JOptionPane.showInputDialog(null, "Server host name?", "Client Startup", JOptionPane.QUESTION_MESSAGE);
  if (host != null) {
    val remote = Naming.lookup("//" + host + "/remoteServer").asInstanceOf[RemoteServer];
    ResourceAllocationExample.makeClient(new ServerFactory {
      def connectToServer(requests: Signal[Int]) = {
        receive(remote.connectToServer(send(requests)))
      }
    }).addWindowListener(new WindowAdapter() {
      override def windowClosed(event: WindowEvent) {
        Reactive.setThreadPoolSize(0);
      }
    });
  }
}