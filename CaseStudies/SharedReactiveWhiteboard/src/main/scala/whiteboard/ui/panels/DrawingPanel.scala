package whiteboard.ui.panels

import java.awt.{Dimension, Color}
import whiteboard.figures.factories.ShapeFactory
import reactive.signals.Signal
import ui.ReactiveComponent
import whiteboard.figures.Shape
import ui.ReactiveComponent.{Drag, Down, MouseEvent}
import reactive.events.EventStream
import java.rmi.Naming
import whiteboard.WhiteboardServer.RemoteWhiteboard
import reactive.remote.RemoteReactives
import reactive.remote.impl.{RemoteSignalSourceImpl, RemoteEventSourceImpl, RemoteSignalSinkImpl}
import javax.swing.JOptionPane
import whiteboard.{ShapeCommand, Command}

class DrawingPanel(
  val nextShapeFactory: Signal[ShapeFactory], 
  val nextStrokeWidth: Signal[Int], 
  val nextColor: Signal[Color],
  val clearCommandStream: EventStream[Command]
) extends ReactiveComponent(new ShapePanel) {
  asComponent.setPreferredSize(new Dimension(200, 200))

  val constructingShape: Signal[Option[Shape]] = mouseEvents.single.fold[Option[Shape]](None) {
    (currentShape: Option[Shape], event: MouseEvent) =>
      event match {
        case Down(point) => Some(nextShapeFactory.single.now.nextShape(nextStrokeWidth.single.now, nextColor.single.now, List(point)))
        case Drag(from, to) => Some(currentShape.get.copy(currentShape.get.strokeWidth, currentShape.get.color, to :: currentShape.get.mousePath))
        case _ => None
      }
  }

  val newShapes: EventStream[Shape] = constructingShape.single.delta.single.collect { case (Some(shape), None) => shape }
  val newShapesCommands: EventStream[Command] = newShapes.single.map[Command] { ShapeCommand }.single.merge(clearCommandStream)

  val serverHostName = JOptionPane.showInputDialog(null, "Please enter server host name:", "Connect", JOptionPane.QUESTION_MESSAGE)
  
  val remoteWhiteboard = Naming.lookup("//"+serverHostName+"/remoteWhiteboard").asInstanceOf[RemoteWhiteboard]
  val shapes = remoteWhiteboard.connectShapes(new RemoteEventSourceImpl(newShapesCommands), Some(new RemoteSignalSourceImpl(constructingShape)))

  val shapesRemote = new RemoteSignalSinkImpl(shapes)
  asComponent.shapes << shapesRemote

  def disconnect() = {
    shapesRemote.disconnect()
  }
 }
