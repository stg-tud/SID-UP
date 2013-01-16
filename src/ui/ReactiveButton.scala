package ui
import java.awt.event.ActionEvent
import javax.swing.JButton
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.Reactive
import reactive.Var

class ReactiveButton(val text: Reactive[String], val enabled: Reactive[Boolean] = Var(true)) extends {
  private val _realButton = new JButton(text.value)
  val asComponent: JComponent = _realButton
} with ReactiveCommitable {
  //  private val _clicks  = EventSource[ActionEvent]
  override protected val observeWhileVisible = List(
    new ReactiveAndObserverPair(enabled, { value: Boolean =>
      AWTThreadSafe {
        _realButton.setEnabled(value)
      }
    }),
    new ReactiveAndObserverPair(text, { value: String =>
      AWTThreadSafe {
        _realButton.setText(value)
      }
    }));
  //  _realButton.addActionListener(new ActionListener(){
  //    override def actionPerformed(event : ActionEvent) = {
  //      _clicks << event
  //    }
  //  })

  //  val commits : Events[ActionEvent] = _clicks
}