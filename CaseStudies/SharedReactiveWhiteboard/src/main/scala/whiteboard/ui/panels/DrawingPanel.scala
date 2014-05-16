package whiteboard.ui.panels

import java.awt.{Dimension, Color}
import whiteboard.figures.factories.ShapeFactory
import reactive.signals.Signal
import ui.ReactiveComponent
import whiteboard.figures.Shape
import ui.ReactiveComponent.{Drag, Down, MouseEvent}
import reactive.events.EventStream
import whiteboard.Command

class DrawingPanel(
  val nextShapeFactory: Signal[ShapeFactory],
  val nextStrokeWidth: Signal[Int],
  val nextColor: Signal[Color],
  val clearCommandStream: EventStream[Command]
) extends ReactiveComponent(new ShapePanel) {
  asComponent.setPreferredSize(new Dimension(200, 200))

  val constructingShape: Signal[Option[Shape]] = mouseEvents.fold[Option[Shape]](None) {
    (currentShape: Option[Shape], event: MouseEvent) =>
      event match {
        case Down(point) => Some(nextShapeFactory.now.nextShape(nextStrokeWidth.now, nextColor.now, List(point)))
        case Drag(from, to) => Some(currentShape.get.copy(currentShape.get.strokeWidth, currentShape.get.color, to :: currentShape.get.mousePath))
        case _ => None
      }
  }

  val newShapes: EventStream[Shape] = constructingShape.delta.collect { case (Some(shape), None) => shape }

  val shapes = asComponent.shapes
 }
