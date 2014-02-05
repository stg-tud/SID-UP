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
    }
  }
}
