package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStreamIdentifier: String): String
  }
  class RemoteWhiteboardImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStreamIdentifier: String): String = {
      val shapeStream = RemoteReactives.lookupEvent[Shape](shapeStreamIdentifier)
      val allShapes = shapeStream.log

      RemoteReactives.rebind("shapeList", allShapes)
      "shapeList"
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", new RemoteWhiteboardImpl)

  while (true) {
    Thread.sleep(1000)
  }
}
