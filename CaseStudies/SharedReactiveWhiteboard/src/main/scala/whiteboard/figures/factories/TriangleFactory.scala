package whiteboard.figures.factories

import whiteboard.figures.Triangle
import java.awt.{Point, Color}

class TriangleFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Triangle(strokeWidth, color, mousePath)

  override def nextShape = new Triangle
}
