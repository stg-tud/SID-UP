package whiteboard

import java.rmi.server.UnicastRemoteObject
import reactive.remote.RemoteReactives
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.{ EventStream, TransposeEventStream }
import reactive.signals.{ TransposeSignal, Signal, Var }
import reactive.remote.impl.{ RemoteSignalSinkImpl, RemoteEventSinkImpl, RemoteSignalSourceImpl }
import reactive.remote.RemoteDependency
import reactive.remote.RemoteSignalDependency
import scala.concurrent.stm._
import reactive.Lift.single._

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStream: RemoteDependency[Command], currentShape: Option[RemoteSignalDependency[Option[Shape]]] = None): RemoteSignalDependency[Iterable[Shape]]
  }

  val allClientShapeCommands = Var(Seq.empty[EventStream[Command]])
  val allClientShapeCommandsTransposeStream = atomic { new TransposeEventStream[Command](allClientShapeCommands, _) }
  val allClientShapeCommandsHeadStream = allClientShapeCommandsTransposeStream.single.map { _.head }
  val persistentShapes = allClientShapeCommandsHeadStream.single.fold(List.empty[Shape]) { (list, cmd) =>
    cmd match {
      case ShapeCommand(shape) => shape :: list
      case ClearCommand => List.empty
    }
  }

  val allClientsCurrentShape = Var(Seq.empty[Signal[Option[Shape]]])
  val currentShapes = atomic { tx => new TransposeSignal[Option[Shape]](allClientsCurrentShape, tx).map { _.flatten }(tx) }

  def ++[T]: (Iterable[T], Iterable[T]) => Iterable[T] = { (first, second) => first ++ second }
  val shapes = ++[Shape](currentShapes, persistentShapes)

  val shapesRemote = new RemoteSignalSourceImpl(shapes)

  object remoteImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStream: RemoteDependency[Command], currentShape: Option[RemoteSignalDependency[Option[Shape]]] = None) = {
      println("new client connecting: " + shapeStream)
      val newClientShapeStream = new RemoteEventSinkImpl(shapeStream)
      atomic { implicit tx =>
        allClientShapeCommands << allClientShapeCommands.now :+ newClientShapeStream
        currentShape.foreach { currentShapeSignal =>
          val newClientCurrentShapeSignal = new RemoteSignalSinkImpl(currentShapeSignal)
          allClientsCurrentShape << allClientsCurrentShape.now :+ newClientCurrentShapeSignal
        }
      }
      shapesRemote
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", remoteImpl)
}
