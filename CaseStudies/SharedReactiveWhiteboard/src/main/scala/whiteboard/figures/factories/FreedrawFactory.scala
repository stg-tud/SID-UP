package whiteboard.figures.factories

import whiteboard.figures.Freedraw
import java.awt.{Point, Color}

class FreedrawFactory extends ShapeFactory {
  override def nextShape(strokeWidth: Int, color: Color, mousePath: List[Point]) =
    new Freedraw(strokeWidth, color, mousePath)

  override def nextShape = new Freedraw
}
