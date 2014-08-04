package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Freedraw

class FreedrawFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Freedraw(strokeWidth, color, mousePath)

  override def nextShape = new Freedraw
}
