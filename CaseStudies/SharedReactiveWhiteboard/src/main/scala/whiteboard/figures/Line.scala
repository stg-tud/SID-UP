package whiteboard.figures

import java.awt.{Graphics2D, Point, Color}

case class Line(
  override val strokeWidth: Int = 1,
  override val color: Color = Color.BLACK,
  override val mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) =
    g.drawLine(start.x, start.y, end.x, end.y)

  override def copy(strokeWidth: Int, color: Color, mousePath: List[Point]) = new Line(strokeWidth, color, mousePath)
}
