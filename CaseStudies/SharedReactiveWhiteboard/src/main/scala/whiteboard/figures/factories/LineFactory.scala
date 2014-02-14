package whiteboard.figures.factories

import whiteboard.figures.Line
import java.awt.{Point, Color}

class LineFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Line(strokeWidth, color, mousePath)

  override def nextShape = new Line
}
