package whiteboard.figures

import java.awt.{BasicStroke, Graphics2D, Point, Color}
import reactive.signals.Val

abstract class Shape(
  var strokeWidth: Int = 1,
  var color: Color = Color.BLACK,
  var mousePath: List[Point] = List.empty
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
}
