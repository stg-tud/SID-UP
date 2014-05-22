package whiteboard.ui.panels

import javax.swing.{BoxLayout, JPanel}
import ui.ReactiveButton
import reactive.Lift.single._
import whiteboard.figures.factories._
import whiteboard.{Command, ClearCommand}

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

  val setLineFactory = lineButton.commits.single.map { _ => new LineFactory }
  val setRectangleFactory = rectangleButton.commits.single.map { _ => new RectangleFactory }
  val setOvalFactory = ovalButton.commits.single.map { _ => new OvalFactory }
  val setTriangleFactory = triangleButton.commits.single.map { _ => new TriangleFactory }
  val setFreedrawFactory = freeDrawButton.commits.single.map { _ => new FreedrawFactory }

  val setShapeFactory = setLineFactory.single.merge(setRectangleFactory, setOvalFactory, setTriangleFactory, setFreedrawFactory)
  val nextShapeFactory = setShapeFactory.single.hold(new LineFactory)

  val clearCommands = clearButton.commits.single.map[Command] { _ => ClearCommand }
}
