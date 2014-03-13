package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.{EventStream, TransposeEventStream}
import reactive.signals.Var

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStreamIdentifier: String): String
    def connectCurrentShape(currentShapeIdentifier: String): String
  }
  class RemoteWhiteboardImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStreamIdentifier: String): String = {
      val newClientShapeStream = RemoteReactives.lookupEvent[Shape](shapeStreamIdentifier)
      allClientShapes << allClientShapes.now :+ newClientShapeStream

      "shapes"
    }

    override def connectCurrentShape(currentShapeIdentifier: String): String = {
      val newClientCurrentShapeStream = RemoteReactives.lookupEvent[Option[Shape]](currentShapeIdentifier)
      allClientsCurrentShape << allClientsCurrentShape.now :+ newClientCurrentShapeStream

      "currentShape"
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", new RemoteWhiteboardImpl)

  val allClientShapes = Var(Seq.empty[EventStream[Shape]])
  val allClientShapesTransposeStream = new TransposeEventStream[Shape](allClientShapes)
  val allClientShapesHeadStream = allClientShapesTransposeStream.map { _.head }
  val shapes = allClientShapesHeadStream.fold[List[Shape]](List.empty[Shape]) { (list, shape) => shape :: list }
  RemoteReactives.rebind("shapes", shapes)

  val allClientsCurrentShape = Var(Seq.empty[EventStream[Option[Shape]]])
  val allClientCurrentShapeTransposeStream = new TransposeEventStream[Option[Shape]](allClientsCurrentShape)
  val allClientCurrentShapeHeadStream = allClientCurrentShapeTransposeStream.map { _.head }
  val currentShape =  allClientCurrentShapeHeadStream hold None
  RemoteReactives.rebind("currentShape", currentShape)

  while (true) {
    Thread.sleep(1000)
  }
}
