package whiteboard.figures

import java.awt.{Graphics2D, Point, Color}

class Freedraw(
  strokeWidth: Int = 1,
  color: Color = Color.BLACK,
  mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) = {
    if ((this: Shape).mousePath.size > 1) {
      // Create a list containing tuples of those points which must be connected with a line
      val lines = (this: Shape).mousePath zip (this: Shape).mousePath.tail

      for ((a, b) <- lines)
        g.drawLine(a.x, a.y, b.x, b.y)
    }
  }
}
