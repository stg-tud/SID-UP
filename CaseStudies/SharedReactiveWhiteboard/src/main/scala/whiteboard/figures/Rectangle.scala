package whiteboard.figures

import java.awt.{Color, Graphics2D, Point}

case class Rectangle(
  override val strokeWidth: Int = 1,
  override val color: Color = Color.BLACK,
  override val mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) = {
    val x = math.min(start.x, end.x)
    val y = math.min(start.y, end.y)
    val width = math.abs(start.x - end.x)
    val height = math.abs(start.y - end.y)

    g.drawRect(x, y, width, height)
  }

  override def copy(strokeWidth: Int, color: Color, mousePath: List[Point]) = new Rectangle(strokeWidth, color, mousePath)
}
