package whiteboard

import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{BorderLayout, Dimension}
import javax.swing._

import whiteboard.ui.panels.{DrawingPanel, ShapeSelectionPanel, StrokeInputPanel}


object Whiteboard {
  val strokeInputPanel = new StrokeInputPanel
  val shapeSelectionPanel = new ShapeSelectionPanel
  val drawingPanel = new DrawingPanel(
    shapeSelectionPanel.nextShapeFactory,
    strokeInputPanel.nextStrokeWidth,
    strokeInputPanel.nextColor,
    shapeSelectionPanel.clearCommands
  )

  def main(args: Array[String]): Unit = {
    makeWindow("Whiteboard", 1000, 600)(
      drawingPanel.asComponent -> BorderLayout.CENTER,
      strokeInputPanel -> BorderLayout.NORTH,
      shapeSelectionPanel -> BorderLayout.WEST
    ).addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) = {
        drawingPanel.disconnect()
      }
    })
  }

  def makeWindow(name: String, width: Int, height: Int)(components: (JComponent, String)*) = {
    val window = new JFrame(name)
    window.setPreferredSize(new Dimension(width, height))
    window.setLayout(new BorderLayout())

    for ((component, direction) <- components) {
      window.add(component, direction)
    }

    window.pack()
    window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    window.setVisible(true)
    window
  }
}
