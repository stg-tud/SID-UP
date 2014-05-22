package whiteboard.ui.panels

import javax.swing.JPanel
import java.awt.{ Graphics2D, Color, Graphics }
import reactive.Lift.single._
import reactive.signals.{ RoutableVar, Signal }
import whiteboard.figures.Shape

class ShapePanel extends JPanel {
  val shapes = RoutableVar(Iterable.empty[Shape])

  // Repaint when a new shape was added
  shapes.single.observe { _ => repaint() }

  override def paintComponent(g: Graphics) {
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    g.setColor(Color.BLACK)

    for (shape <- shapes.single.now.toSeq.reverse)
      shape.draw(g.asInstanceOf[Graphics2D])
  }
}
