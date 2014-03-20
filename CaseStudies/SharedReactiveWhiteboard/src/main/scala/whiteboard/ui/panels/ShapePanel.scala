package whiteboard.ui.panels

import javax.swing.JPanel
import java.awt.{Graphics2D, Color, Graphics}
import reactive.Lift._
import reactive.signals.{RoutableVar, Signal}
import whiteboard.figures.Shape

class ShapePanel extends JPanel {
  var currentShape = RoutableVar[Option[Shape]](None)
  var shapes = RoutableVar(List.empty[Shape])

  override def paintComponent(g: Graphics) {
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    g.setColor(Color.BLACK)

    if (currentShape.now.isDefined)
      currentShape.now.get.draw(g.asInstanceOf[Graphics2D])

    for (shape <- shapes.now.reverse)
      shape.draw(g.asInstanceOf[Graphics2D])
  }
}
