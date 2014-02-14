package whiteboard.figures.factories

import whiteboard.figures.Oval
import java.awt.{Point, Color}

class OvalFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Oval(strokeWidth, color, mousePath)

  override def nextShape = new Oval
}
