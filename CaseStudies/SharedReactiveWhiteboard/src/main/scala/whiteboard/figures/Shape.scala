package whiteboard.figures

import java.awt.{BasicStroke, Graphics2D, Point, Color}
import reactive.signals.Val

abstract class Shape(
  val strokeWidth: Int = 1,
  val color: Color = Color.BLACK,
  val mousePath: List[Point] = List.empty
) {
  def start = if (mousePath.isEmpty) null else mousePath.head
  def end = if (mousePath.isEmpty) null else mousePath.last

  def draw(g: Graphics2D) = {
    if (start != null && end != null) {
      val stroke = new BasicStroke(strokeWidth)

      g.setStroke(stroke)
      g.setColor(color)
      doDraw(g)
    }
  }

  def doDraw(g: Graphics2D)
  def copy(strokeWidth: Int, color: Color, mousePath: List[Point]): Shape
}
