package whiteboard.figures

import java.awt.{Graphics2D, Point, Color}

case class Triangle(
  override val strokeWidth: Int = 1,
  override val color: Color = Color.BLACK,
  override val mousePath: List[Point] = List.empty
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
