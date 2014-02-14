package whiteboard.figures.factories

import whiteboard.figures.Rectangle
import java.awt.{Point, Color}

class RectangleFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Rectangle(strokeWidth, color, mousePath)

  override def nextShape = new Rectangle
}
