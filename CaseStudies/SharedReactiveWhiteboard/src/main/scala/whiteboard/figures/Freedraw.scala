package whiteboard.figures

import java.awt.{Color, Graphics2D, Point}

case class Freedraw(
  override val strokeWidth: Int = 1,
  override val color: Color = Color.BLACK,
  override val mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) = {
    if ((this: Shape).mousePath.size > 1) {
      // Create a list containing tuples of those points which must be connected with a line
      val lines = (this: Shape).mousePath zip (this: Shape).mousePath.tail

      for ((a, b) <- lines)
        g.drawLine(a.x, a.y, b.x, b.y)
    }
  }

  override def copy(strokeWidth: Int, color: Color, mousePath: List[Point]) = new Freedraw(strokeWidth, color, mousePath)
}
