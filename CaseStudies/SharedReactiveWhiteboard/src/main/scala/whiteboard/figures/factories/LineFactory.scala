package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Line

class LineFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Line(strokeWidth, color, mousePath)

  override def nextShape = new Line
}
