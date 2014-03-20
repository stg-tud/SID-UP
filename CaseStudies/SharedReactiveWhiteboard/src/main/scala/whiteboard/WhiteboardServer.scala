package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.{ EventStream, TransposeEventStream }
import reactive.signals.Var
import reactive.remote.impl.RemoteEventSinkImpl
import reactive.remote.RemoteDependency
import reactive.remote.impl.RemoteSignalSourceImpl
import reactive.remote.RemoteSignalDependency

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStreamIdentifier: RemoteDependency[Shape]): RemoteSignalDependency[List[Shape]]
    def connectCurrentShape(currentShapeIdentifier: RemoteDependency[Option[Shape]]): RemoteSignalDependency[Option[Shape]]
  }
  
  val allClientShapes = Var(Seq.empty[EventStream[Shape]])
  val allClientShapesTransposeStream = new TransposeEventStream[Shape](allClientShapes)
  val allClientShapesHeadStream = allClientShapesTransposeStream.map { _.head }
  val shapes = allClientShapesHeadStream.fold[List[Shape]](List.empty[Shape]) { (list, shape) => shape :: list }
  val shapesRemote = new RemoteSignalSourceImpl(shapes)

  val allClientsCurrentShape = Var(Seq.empty[EventStream[Option[Shape]]])
  val allClientCurrentShapeTransposeStream = new TransposeEventStream[Option[Shape]](allClientsCurrentShape)
  val allClientCurrentShapeHeadStream = allClientCurrentShapeTransposeStream.map { _.head }
  val currentShape = allClientCurrentShapeHeadStream hold None
  val currentShapeRemote = new RemoteSignalSourceImpl(currentShape)

  object remoteImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStreamIdentifier: RemoteDependency[Shape]) = {
      println("new client connecting: "+shapeStreamIdentifier);
      val newClientShapeStream = new RemoteEventSinkImpl(shapeStreamIdentifier)
      allClientShapes << allClientShapes.now :+ newClientShapeStream
      shapesRemote
    }

    override def connectCurrentShape(currentShapeIdentifier: RemoteDependency[Option[Shape]]) = {
      val newClientCurrentShapeStream = new RemoteEventSinkImpl(currentShapeIdentifier)
      allClientsCurrentShape << allClientsCurrentShape.now :+ newClientCurrentShapeStream
      currentShapeRemote
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", remoteImpl)
}
