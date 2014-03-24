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
import reactive.remote.impl.RemoteEventSourceImpl
import reactive.remote.impl.RemoteSignalSinkImpl
import javax.swing.JOptionPane

class DrawingPanel(
  val nextShapeFactory: Signal[ShapeFactory], 
  val nextStrokeWidth: Signal[Int], 
  val nextColor: Signal[Color]
) extends ReactiveComponent(new ShapePanel) {
  asComponent.setPreferredSize(new Dimension(200, 200))

  val constructingShape: Signal[Option[Shape]] = mouseEvents.fold[Option[Shape]](None) {
    (currentShape: Option[Shape], event: MouseEvent) =>
      event match {
        case Down(point) => Some(nextShapeFactory.now.nextShape(nextStrokeWidth.now, nextColor.now, List(point)))
        case Drag(from, to) => Some(currentShape.get.copy(currentShape.get.strokeWidth, currentShape.get.color, to :: currentShape.get.mousePath))
        case _ => currentShape
      }
  }

  val newShapes : EventStream[Shape] =
    constructingShape.pulse(mouseUps).filter { option => option.isDefined }.map { option => option.get }

  val serverHostName = JOptionPane.showInputDialog(null, "Please enter server host name:", "Connect", JOptionPane.QUESTION_MESSAGE)
  
  val remoteWhiteboard = Naming.lookup("//"+serverHostName+"/remoteWhiteboard").asInstanceOf[RemoteWhiteboard]
  val shapeListIdentifier = remoteWhiteboard.connectShapes(new RemoteEventSourceImpl(newShapes))
  
  val asdf = new RemoteSignalSinkImpl(shapeListIdentifier)
  asComponent.shapes << asdf
  
  val currentShapeStream = constructingShape.changes merge mouseUps.map( _ => None)

  val currentShapeIdentifier = remoteWhiteboard.connectCurrentShape(new RemoteEventSourceImpl(currentShapeStream))
  val currentShape = new RemoteSignalSinkImpl(currentShapeIdentifier)
  asComponent.currentShape << currentShape

  // Repaint when current shape changes
  currentShape.changes.observe { _ => asComponent.repaint() }

  // Repaint when a new shape was added
  asComponent.shapes.changes.observe { _ => asComponent.repaint() }
}
