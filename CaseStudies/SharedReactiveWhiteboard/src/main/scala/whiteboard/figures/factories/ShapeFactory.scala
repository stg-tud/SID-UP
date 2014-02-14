package whiteboard.figures.factories

import whiteboard.figures.Shape
import java.awt.{Point, Color}

abstract class ShapeFactory {
  def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]): Shape
  def nextShape: Shape
}
