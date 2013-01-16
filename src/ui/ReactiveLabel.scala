package ui

import javax.swing.JLabel
import javax.swing.JComponent
import reactive.Reactive
import javax.swing.ImageIcon
import javax.swing.SwingConstants
import java.awt.EventQueue
import javax.swing.event.AncestorListener
import javax.swing.event.AncestorEvent

class ReactiveLabel(text: Reactive[_]) extends {
  private val label = new JLabel(String.valueOf(text.value), ReactiveLabel.icon(text.dirty.value), SwingConstants.LEFT)
  override val asComponent: JComponent = label;
} with ReactiveComponent {
  override protected val observeWhileVisible = List(
    new ReactiveAndObserverPair(text, { value: Any =>
      AWTThreadSafe {
        label.setText(String.valueOf(value))
      }
    }),
    new ReactiveAndObserverPair(text.dirty, { value: Boolean =>
      AWTThreadSafe {
        label.setIcon(ReactiveLabel.icon(value))
      }
    }));
}

object ReactiveLabel {
  private val check = new ImageIcon(getClass().getClassLoader().getResource("check.png"))
  private val hourglass = new ImageIcon(getClass().getClassLoader().getResource("hourglass.gif"))
  def icon(dirty : Boolean) = if(dirty) hourglass else check
}