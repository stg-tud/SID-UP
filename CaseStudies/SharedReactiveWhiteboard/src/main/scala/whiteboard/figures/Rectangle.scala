package whiteboard.figures

import java.awt.{Graphics2D, Point, Color}

class Rectangle(
  strokeWidth: Int = 1,
  color: Color = Color.BLACK,
  mousePath: List[Point] = List.empty
) extends Shape(strokeWidth, color, mousePath) {
  override def doDraw(g: Graphics2D) = {
    val x = math.min(start.x, end.x)
    val y = math.min(start.y, end.y)
    val width = math.abs(start.x - end.x)
    val height = math.abs(start.y - end.y)

    g.drawRect(x, y, width, height)
  }
}
