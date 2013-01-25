package ui
import java.awt.event.ActionEvent
import javax.swing.JButton
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.Reactive
import reactive.Var
import reactive.Signal
import reactive.EventStream
import reactive.EventSource

class ReactiveButton(val text: Signal[String], val enabled: Signal[Boolean] = Var(true)) extends {
  private val _realButton = new JButton(text.now)
  val asComponent: JComponent = _realButton
} with ReactiveCommitable {
    private val _clicks  = EventSource[ActionEvent]
  override protected val observeWhileVisible = List(
    observeInEDT(enabled) { _realButton.setEnabled(_) },
    observeInEDT(text) { _realButton.setText(_) });
    _realButton.addActionListener(new ActionListener(){
      override def actionPerformed(event : ActionEvent) = {
        _clicks << event
      }
    })

    val commits : EventStream[ActionEvent] = _clicks
}