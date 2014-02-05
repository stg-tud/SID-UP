package whiteboard.figures

import reactive.signals.{Val, Signal}
import java.awt.Color

class ShapeFactory {
  var nextShape: String = "line"
  var strokeWidth: Int = 1
  var color: Color = Color.BLACK

  def makeShape: Shape = {
    nextShape match {
      case "line" => new Line(strokeWidth, color, List.empty)
      case "rectangle" => new Rectangle(strokeWidth, color, List.empty)
      case "triangle" => new Triangle(strokeWidth, color, List.empty)
      case "oval" => new Oval(strokeWidth, color, List.empty)
      case "freedraw" => new Freedraw(strokeWidth, color, List.empty)
    }
  }
}
