package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Triangle

class TriangleFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Triangle(strokeWidth, color, mousePath)

  override def nextShape = new Triangle
}
