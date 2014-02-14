package whiteboard.ui.panels

import javax.swing.JPanel
import java.awt.{Graphics2D, Color, Graphics}
import reactive.signals.{Var, Signal}
import whiteboard.figures.Shape

class ShapePanel extends JPanel {
  var currentShape: Signal[Option[Shape]] = Var(None)
  var shapes: Signal[List[Shape]] = Var(List.empty)

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
