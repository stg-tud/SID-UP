package whiteboard

import javax.swing._
import java.awt.{Dimension, BorderLayout}


object Whiteboard {
  def main(args: Array[String]): Unit = {
    makeWindow("Whiteboard", 1000, 600)(
      new JTabbedPane -> BorderLayout.CENTER
    )
  }

  def makeWindow(name: String, width: Int, height: Int)(components: (JComponent, String)*) = {
    val window = new JFrame(name)
    window.setPreferredSize(new Dimension(width, height))
    window.setLayout(new BorderLayout())

    for ((component, direction) <- components) {
      window.add(component, direction)
    }

    window.pack()
    window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    window.setVisible(true)
    window
  }
}
