package whiteboard.ui.panels

import javax.swing.{BoxLayout, JPanel}
import ui.ReactiveButton
import reactive.Lift._

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

  // TODO Replace println with real code
  lineButton.commits.map { _ => println("Set nextShape to 'Line'") }
  rectangleButton.commits.map { _ => println("Set nextShape to 'Rectangle'") }
  ovalButton.commits.map { _ => println("Set nextShape to 'Oval'") }
  triangleButton.commits.map { _ => println("Set nextShape to 'Triangle'") }
  freeDrawButton.commits.map { _ => println("Set nextShape to 'Free Draw'") }
}
