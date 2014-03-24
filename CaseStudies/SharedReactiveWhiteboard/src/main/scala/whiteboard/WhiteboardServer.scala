package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.{ EventStream, TransposeEventStream }
import reactive.signals.{TransposeSignal, Signal, Var}
import reactive.remote.impl.{RemoteSignalSinkImpl, RemoteEventSinkImpl, RemoteSignalSourceImpl}
import reactive.remote.RemoteDependency
import reactive.remote.RemoteSignalDependency

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStream: RemoteDependency[Shape]): RemoteSignalDependency[List[Shape]]
    def connectCurrentShape(currentShapeSignal: RemoteSignalDependency[Option[Shape]]): RemoteSignalDependency[Iterable[Option[Shape]]]
  }
  
  val allClientShapes = Var(Seq.empty[EventStream[Shape]])
  val allClientShapesTransposeStream = new TransposeEventStream[Shape](allClientShapes)
  val allClientShapesHeadStream = allClientShapesTransposeStream.map { _.head }
  val shapes = allClientShapesHeadStream.fold[List[Shape]](List.empty[Shape]) { (list, shape) => shape :: list }
  val shapesRemote = new RemoteSignalSourceImpl(shapes)

  val allClientsCurrentShape = Var(Seq.empty[Signal[Option[Shape]]])
  val currentShape = new TransposeSignal[Option[Shape]](allClientsCurrentShape)
  val currentShapeRemote = new RemoteSignalSourceImpl(currentShape)

  object remoteImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStream: RemoteDependency[Shape]) = {
      println("new client connecting: "+shapeStream)
      val newClientShapeStream = new RemoteEventSinkImpl(shapeStream)
      allClientShapes << allClientShapes.now :+ newClientShapeStream
      shapesRemote
    }

    override def connectCurrentShape(currentShapeSignal: RemoteSignalDependency[Option[Shape]]) = {
      val newClientCurrentShapeStream = new RemoteSignalSinkImpl(currentShapeSignal)
      allClientsCurrentShape << allClientsCurrentShape.now :+ newClientCurrentShapeStream
      currentShapeRemote
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", remoteImpl)
}
