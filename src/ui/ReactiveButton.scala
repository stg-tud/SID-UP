package ui
import java.awt.event.ActionEvent
import javax.swing.JButton
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.Reactive
import reactive.Var

class ReactiveButton(val text: Reactive[String], val enabled: Reactive[Boolean] = Var(true)) extends ReactiveCommitable {
  //  private val _clicks  = EventSource[ActionEvent]
  private val _realButton = new JButton(text.value)
  _realButton.setEnabled(enabled.value)
  enabled.observe { value =>
    AWTThreadSafe {
      _realButton.setEnabled(value)
    }
  }
  text.observe { value =>
    AWTThreadSafe {
      _realButton.setText(value)
    }
  }
  //  _realButton.addActionListener(new ActionListener(){
  //    override def actionPerformed(event : ActionEvent) = {
  //      _clicks << event
  //    }
  //  })

  //  val commits : Events[ActionEvent] = _clicks
  val asComponent: JComponent = _realButton
}