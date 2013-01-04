package ui

import javax.swing.JLabel
import javax.swing.JComponent
import reactive.Reactive
import javax.swing.ImageIcon
import javax.swing.SwingConstants
import java.awt.EventQueue

class ReactiveLabel(text: Reactive[_]) extends ReactiveComponent {
  private val check = new ImageIcon(getClass().getClassLoader().getResource("check.png"))
  private val hourglass = new ImageIcon(getClass().getClassLoader().getResource("hourglass.gif"))
  private val label = new JLabel(String.valueOf(text.value), if (text.dirty) hourglass else check, SwingConstants.LEFT)
  text.observe { value =>
    AWTThreadSafe {
      label.setText(String.valueOf(value))
    }
  }
  text.dirty.observe { value =>
    AWTThreadSafe {
      label.setIcon(if (value) hourglass else check)
    }
  }
  val asComponent: JComponent = label;
}
