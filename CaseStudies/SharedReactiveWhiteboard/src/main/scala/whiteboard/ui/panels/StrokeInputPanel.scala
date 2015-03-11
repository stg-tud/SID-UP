package whiteboard.ui.panels

import javax.swing._
import java.awt.{Color, FlowLayout}
import reactive.lifting.Lift._
import ui.ReactiveButton
import ui.ReactiveSpinner
import javax.swing.event.{ChangeEvent, ChangeListener}
import reactive.signals.Var

class StrokeInputPanel extends JPanel(new FlowLayout) {
  private val colorWindow = new ColorWindow
  private val spinner = new ReactiveSpinner(1)

  private val showColorWindow = new ReactiveButton("Show Colorinput")
  showColorWindow.commits.observe { _ => colorWindow.setVisible(!colorWindow.isVisible)}

  add(new JLabel("stroke width: "))
  add(spinner.asComponent)
  add(showColorWindow.asComponent)

  val nextStrokeWidth = spinner.value
  val nextColor = colorWindow.color
}

class ColorWindow extends JFrame("Choose Color") {
  private val colorChooser = new JColorChooser()
  private val model = colorChooser.getSelectionModel

  private val closeButton = new ReactiveButton("OK")
  closeButton.commits.observe { _ =>  setVisible(false) }

  val color: Var[Color] = Var(Color.BLACK)

  model.addChangeListener(new ChangeListener {
    override def stateChanged(event: ChangeEvent) =
      color << model.getSelectedColor
  })

  private val panel = new JPanel()
  panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
  panel.add(colorChooser)
  panel.add(closeButton.asComponent)
  setContentPane(panel)
  pack()
}
