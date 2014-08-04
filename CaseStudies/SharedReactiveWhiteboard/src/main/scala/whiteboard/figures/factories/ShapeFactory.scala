package whiteboard.figures.factories

import java.awt.{Color, Point}

import whiteboard.figures.Shape

abstract class ShapeFactory {
  def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]): Shape
  def nextShape: Shape
}
