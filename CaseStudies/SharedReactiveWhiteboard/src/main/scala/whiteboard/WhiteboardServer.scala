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
    def connectShapes(shapeStream: RemoteDependency[Command]): RemoteSignalDependency[List[Shape]]
    def connectCurrentShape(currentShapeSignal: RemoteSignalDependency[Option[Shape]]): RemoteSignalDependency[Iterable[Option[Shape]]]
  }
  
  val allClientShapeCommands = Var(Seq.empty[EventStream[Command]])
  val allClientShapeCommandsTransposeStream = new TransposeEventStream[Command](allClientShapeCommands)
  val allClientShapeCommandsHeadStream = allClientShapeCommandsTransposeStream.map { _.head }
  val shapeCommands = allClientShapeCommandsHeadStream.fold[List[Command]](List.empty[Command]) { (list, cmd) => cmd :: list }
  val shapes = shapeCommands.map { clearCommandList }.map { _.map {_.shape}}
  val shapesRemote = new RemoteSignalSourceImpl(shapes)

  val allClientsCurrentShape = Var(Seq.empty[Signal[Option[Shape]]])
  val currentShape = new TransposeSignal[Option[Shape]](allClientsCurrentShape)
  val currentShapeRemote = new RemoteSignalSourceImpl(currentShape)

  object remoteImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStream: RemoteDependency[Command]) = {
      println("new client connecting: "+shapeStream)
      val newClientShapeStream = new RemoteEventSinkImpl(shapeStream)
      allClientShapeCommands << allClientShapeCommands.now :+ newClientShapeStream
      shapesRemote
    }

    override def connectCurrentShape(currentShapeSignal: RemoteSignalDependency[Option[Shape]]) = {
      val newClientCurrentShapeStream = new RemoteSignalSinkImpl(currentShapeSignal)
      allClientsCurrentShape << allClientsCurrentShape.now :+ newClientCurrentShapeStream
      currentShapeRemote
    }
  }

  def clearCommandList(l: List[Command]): List[ShapeCommand] = {
    l.foldRight[List[ShapeCommand]](List.empty) { (cmd, list) => cmd match {
      case ShapeCommand(_) => cmd.asInstanceOf[ShapeCommand] :: list
      case ClearCommand => List.empty
    }}
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", remoteImpl)
}
