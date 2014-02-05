package whiteboard.ui.panels

import javax.swing.{BoxLayout, JPanel}
import ui.ReactiveButton
import reactive.Lift._
import whiteboard.Whiteboard

class ShapeSelectionPanel extends JPanel() {
  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  val lineButton = new ReactiveButton("Line")
  val rectangleButton = new ReactiveButton("Rectangle")
  val ovalButton = new ReactiveButton("Oval")
  val triangleButton = new ReactiveButton("Triangle")
  val freeDrawButton = new ReactiveButton("Free Draw")

  add(lineButton.asComponent)
  add(rectangleButton.asComponent)
  add(ovalButton.asComponent)
  add(triangleButton.asComponent)
  add(freeDrawButton.asComponent)

  lineButton.commits.map { _ => Whiteboard.shapeFactory.nextShape = "line" }
  rectangleButton.commits.map { _ => Whiteboard.shapeFactory.nextShape = "rectangle" }
  ovalButton.commits.map { _ => Whiteboard.shapeFactory.nextShape = "oval" }
  triangleButton.commits.map { _ => Whiteboard.shapeFactory.nextShape = "triangle" }
  freeDrawButton.commits.map { _ => Whiteboard.shapeFactory.nextShape = "freedraw" }
}
