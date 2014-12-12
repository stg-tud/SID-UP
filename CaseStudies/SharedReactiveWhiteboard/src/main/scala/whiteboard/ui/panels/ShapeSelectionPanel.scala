package whiteboard.ui.panels

import javax.swing.{BoxLayout, JPanel}

import reactive.Lift._
import ui.ReactiveButton
import whiteboard.figures.factories._
import whiteboard.{ClearCommand, Command}

class ShapeSelectionPanel extends JPanel() {
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  val clearButton = new ReactiveButton("Clear")
  val lineButton = new ReactiveButton("Line")
  val rectangleButton = new ReactiveButton("Rectangle")
  val ovalButton = new ReactiveButton("Oval")
  val triangleButton = new ReactiveButton("Triangle")
  val freeDrawButton = new ReactiveButton("Free Draw")

  add(clearButton.asComponent)
  add(lineButton.asComponent)
  add(rectangleButton.asComponent)
  add(ovalButton.asComponent)
  add(triangleButton.asComponent)
  add(freeDrawButton.asComponent)

  val setLineFactory = lineButton.commits.map { _ => new LineFactory }
  val setRectangleFactory = rectangleButton.commits.map { _ => new RectangleFactory }
  val setOvalFactory = ovalButton.commits.map { _ => new OvalFactory }
  val setTriangleFactory = triangleButton.commits.map { _ => new TriangleFactory }
  val setFreedrawFactory = freeDrawButton.commits.map { _ => new FreedrawFactory }

  val setShapeFactory = setLineFactory.merge(setRectangleFactory, setOvalFactory, setTriangleFactory, setFreedrawFactory)
  val nextShapeFactory = setShapeFactory.hold(new LineFactory)

  val clearCommands = clearButton.commits.map[Command] { _ => ClearCommand }
}
