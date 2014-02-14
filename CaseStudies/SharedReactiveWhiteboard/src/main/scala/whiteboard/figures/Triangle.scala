package whiteboard.figures

import java.awt.{Graphics2D, Point, Color}

class Triangle(
  strokeWidth: Int = 1,
  color: Color = Color.BLACK,
  mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) = {
    val a = start
    val b = new Point(start.x, end.y)
    val c = end

    g.drawLine(a.x, a.y, b.x, b.y)
    g.drawLine(b.x, b.y, c.x, c.y)
    g.drawLine(a.x, a.y, c.x, c.y)
  }

  override def copy(strokeWidth: Int, color: Color, mousePath: List[Point]) = new Triangle(strokeWidth, color, mousePath)
}
