package whiteboard.figures

import java.awt.{BasicStroke, Color, Graphics2D, Point}

abstract class Shape(
  val strokeWidth: Int = 1,
  val color: Color = Color.BLACK,
  val mousePath: List[Point] = List.empty
) extends Serializable {
  def start = if (mousePath.isEmpty) null else mousePath.head
  def end = if (mousePath.isEmpty) null else mousePath.last

  def draw(g: Graphics2D) = {
    if (start != null && end != null) {
      val stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER)

      g.setStroke(stroke)
      g.setColor(color)
      doDraw(g)
    }
  }

  def doDraw(g: Graphics2D): Unit
  def copy(strokeWidth: Int, color: Color, mousePath: List[Point]): Shape
}
