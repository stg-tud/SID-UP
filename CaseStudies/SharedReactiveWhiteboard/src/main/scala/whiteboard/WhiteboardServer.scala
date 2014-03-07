package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.impl.DynamicMergeStream

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStreamIdentifier: String): String
    def connectCurrentShape(currentShapeIdentifier: String): String
  }
  class RemoteWhiteboardImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStreamIdentifier: String): String = {
      val newClientShapeStream = RemoteReactives.lookupEvent[Shape](shapeStreamIdentifier)
      allClientsShapeStream.addEvents(newClientShapeStream)

      "shapes"
    }

    override def connectCurrentShape(currentShapeIdentifier: String): String = {
      val newClientCurrentShapeStream = RemoteReactives.lookupEvent[Option[Shape]](currentShapeIdentifier)
      allClientsCurrentShapeStream.addEvents(newClientCurrentShapeStream)

      "currentShape"
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", new RemoteWhiteboardImpl)

  val allClientsShapeStream = new DynamicMergeStream[Shape]()
  val shapes = allClientsShapeStream.log
  RemoteReactives.rebind("shapes", shapes)

  val allClientsCurrentShapeStream = new DynamicMergeStream[Option[Shape]]()
  val currentShape = allClientsCurrentShapeStream hold None
  RemoteReactives.rebind("currentShape", currentShape)

  while (true) {
    Thread.sleep(1000)
  }
}
