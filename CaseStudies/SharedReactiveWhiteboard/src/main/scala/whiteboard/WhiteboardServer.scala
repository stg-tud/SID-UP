package whiteboard

import java.rmi.server.UnicastRemoteObject
import whiteboard.figures.Shape
import java.rmi.Naming
import reactive.events.{ EventStream, TransposeEventStream }
import reactive.signals.{ TransposeSignal, Signal, Var }
import reactive.remote.RemoteDependency
import reactive.remote.RemoteSignalDependency
import reactive.lifting.Lift._
import reactive.mutex.{TransactionLock, TransactionLockImpl}
import java.util.UUID
import javax.swing.JOptionPane

object WhiteboardServer extends App {
  @remote trait RemoteWhiteboard {
    def connectShapes(shapeStream: EventStream[Command], currentShape: Option[Signal[Option[Shape]]] = None): Signal[Iterable[Shape]]
    def lock(): Option[TransactionLock]
  }

  val allClientShapeCommands = Var(Seq.empty[EventStream[Command]])
  val allClientShapeCommandsTransposeStream = allClientShapeCommands.transposeE
  val allClientShapeCommandsHeadStream = allClientShapeCommandsTransposeStream.map { _.head }
  val persistentShapes = allClientShapeCommandsHeadStream.fold(List.empty[Shape]) { (list, cmd) => cmd match {
        case ShapeCommand(shape) => shape :: list
        case ClearCommand => List.empty
  } }

  val allClientsCurrentShape = Var(Seq.empty[Signal[Option[Shape]]])
  val currentShapes = allClientsCurrentShape.transposeS.map{_.flatten}

  def ++[T] : (Iterable[T], Iterable[T]) => Iterable[T] = {(first, second) => first ++ second}
  val shapes = ++[Shape](currentShapes, persistentShapes)

  object remoteImpl extends UnicastRemoteObject with RemoteWhiteboard {
    override def connectShapes(shapeStream: EventStream[Command], currentShape: Option[Signal[Option[Shape]]] = None) = {
      println("new client connecting: " + shapeStream)
      val uuid = UUID.randomUUID()

      if (transactionLock.isDefined)
        transactionLock.get.acquire(uuid)
      allClientShapeCommands << allClientShapeCommands.now :+ shapeStream
      currentShape.foreach { currentShapeSignal =>
        allClientsCurrentShape << allClientsCurrentShape.now :+ currentShapeSignal
      }
      if (transactionLock.isDefined)
        transactionLock.get.release(uuid)

      shapes
    }

    override def lock(): Option[TransactionLock] = {
      transactionLock
    }
  }

  val useLocking =
    JOptionPane.showConfirmDialog(null, "Do you want to use locking?", "Lock", JOptionPane.YES_NO_OPTION)

  val transactionLock = useLocking match {
    case JOptionPane.YES_OPTION => Some(new TransactionLockImpl())
    case JOptionPane.NO_OPTION => None
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteWhiteboard", remoteImpl)
}
