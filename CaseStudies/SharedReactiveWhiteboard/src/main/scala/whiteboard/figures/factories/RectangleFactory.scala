package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Rectangle

class RectangleFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Rectangle(strokeWidth, color, mousePath)

  override def nextShape = new Rectangle
}
