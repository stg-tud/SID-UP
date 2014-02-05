package whiteboard.figures

import reactive.signals.{Var, Val, Signal}
import java.awt.Color

class ShapeFactory {
  var nextShape: Var[String] = Var("line")
  var strokeWidth: Var[Int] = Var(1)
  var color: Var[Color] = Var(Color.BLACK)

  def makeShape: Shape = {
    nextShape.now match {
      case "line" => new Line(strokeWidth.now, color.now, List.empty)
      case "rectangle" => new Rectangle(strokeWidth.now, color.now, List.empty)
      case "triangle" => new Triangle(strokeWidth.now, color.now, List.empty)
      case "oval" => new Oval(strokeWidth.now, color.now, List.empty)
      case "freedraw" => new Freedraw(strokeWidth.now, color.now, List.empty)
    }
  }
}
