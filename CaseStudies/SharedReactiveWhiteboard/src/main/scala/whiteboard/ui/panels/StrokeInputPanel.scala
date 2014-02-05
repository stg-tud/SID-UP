package whiteboard.ui.panels

import javax.swing._
import reactive.signals.{Signal, Val, Var}
import java.awt.{FlowLayout, Color}
import reactive.Lift._
import ui.ReactiveButton
import ui.ReactiveSpinner
import whiteboard.Whiteboard
import whiteboard.figures.ShapeFactory

class StrokeInputPanel extends JPanel(new FlowLayout) {
  private val colorWindow = new ColorWindow
  private val spinner = new ReactiveSpinner(1)

  private val showColorWindow = new ReactiveButton("Show Colorinput")
  showColorWindow.commits.map { _ => colorWindow.setVisible(!colorWindow.isVisible)}

  add(new JLabel("stroke width: "))
  add(spinner.asComponent)
  add(showColorWindow.asComponent)

  spinner.value.changes.map { _ => Whiteboard.shapeFactory.strokeWidth = spinner.value.now }
}

class ColorWindow extends JFrame("Choose Color") {
  private val colorChooser = new JColorChooser()

  private val closeButton = new ReactiveButton("OK")
  closeButton.commits.map { _ => Whiteboard.shapeFactory.color = colorChooser.getColor; setVisible(false) }

  private val panel = new JPanel()
  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  panel.add(colorChooser)
  panel.add(closeButton.asComponent)
  setContentPane(panel)
  pack()
}
