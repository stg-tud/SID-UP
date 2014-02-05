package whiteboard.ui.panels

import whiteboard.figures.{Line, Shape}
import javax.swing.JPanel
import java.awt.{Graphics, Dimension, Color, Graphics2D}
import java.awt.event.{MouseMotionAdapter, MouseEvent, MouseAdapter}
import whiteboard.Whiteboard

class DrawingPanel extends JPanel {
  var currentShape: Shape = null
  var shapes: List[Shape] = List.empty

  setPreferredSize(new Dimension(200, 200))

  override def paintComponent(g: Graphics) {
    g.setColor(Color.WHITE)
    g.fillRect(0, 0, getWidth, getHeight)
    g.setColor(Color.BLACK)

    if (currentShape != null)
      currentShape.draw(g.asInstanceOf[Graphics2D])

    for (shape <- shapes.reverse)
      shape.draw(g.asInstanceOf[Graphics2D])
  }

  addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent) {
      currentShape = Whiteboard.shapeFactory.makeShape
      currentShape.mousePath = List(e.getPoint)
    }

    override def mouseReleased(e: MouseEvent) {
      shapes = currentShape :: shapes
      currentShape = null
      repaint()
    }
  })

  addMouseMotionListener(new MouseMotionAdapter {
    override def mouseDragged(e: MouseEvent) {
      currentShape.mousePath = e.getPoint :: currentShape.mousePath
      repaint()
    }
  })
}
