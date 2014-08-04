package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Oval

class OvalFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Oval(strokeWidth, color, mousePath)

  override def nextShape = new Oval
}
