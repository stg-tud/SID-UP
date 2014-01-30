package whiteboard.ui.panels

import javax.swing._
import reactive.signals.Var
import java.awt.{FlowLayout, Color}
import reactive.Lift._
import ui.ReactiveButton
import ui.ReactiveSpinner

class StrokeInputPanel extends JPanel(new FlowLayout) {
  private val colorWindow = new ColorWindow
  private val spinner = new ReactiveSpinner(1)

  private val showColorWindow = new ReactiveButton("Show Colorinput")
  showColorWindow.commits.map { _ => colorWindow.setVisible(!colorWindow.isVisible)}

  add(new JLabel("stroke width: "))
  add(spinner.asComponent)
  add(showColorWindow.asComponent)

  val strokeWidth = spinner.value
  val color = colorWindow.color
}

class ColorWindow extends JFrame("Choose Color") {
  private val colorChooser = new JColorChooser()

  private val closeButton = new ReactiveButton("OK")
  closeButton.commits.map { _ => color = Var(colorChooser.getColor); setVisible(false) }

  private val panel = new JPanel()
  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  panel.add(colorChooser)
  panel.add(closeButton.asComponent)
  setContentPane(panel)
  pack()

  var color = Var(Color.BLACK)
}
