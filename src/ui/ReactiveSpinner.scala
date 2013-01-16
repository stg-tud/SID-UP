package ui

import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeListener
import javax.swing.event.ChangeEvent
import java.awt.event.ActionEvent
import javax.swing.JFormattedTextField
import javax.swing.text.DefaultFormatter
import java.awt.event.ActionListener
import javax.swing.JComponent
import reactive.Var
import reactive.Reactive

class ReactiveSpinner(initialValue: Int, min: Reactive[Int] = Var(Int.MinValue), max: Reactive[Int] = Var(Int.MaxValue), step: Reactive[Int] = Var(1)) extends {
  private val model = new SpinnerNumberModel(initialValue, min.value, max.value, step.value)
  private val spinner = new JSpinner(model)
  val asComponent: JComponent = spinner
} with ReactiveInput[Int] with ReactiveCommitable {
  protected val observeWhileVisible = List(
    new ReactiveAndObserverPair(min, { value: Int =>
      AWTThreadSafe {
        model.setMinimum(value)
      }
    }),
    new ReactiveAndObserverPair(max, { value: Int =>
      AWTThreadSafe {
        model.setMaximum(value)
      }
    }),
    new ReactiveAndObserverPair(step, { value: Int =>
      AWTThreadSafe {
        model.setStepSize(value)
      }
    }));

  private val _value = Var(model.getValue().asInstanceOf[Int])
  model.addChangeListener(new ChangeListener {
    def stateChanged(event: ChangeEvent) = {
      _value.set(model.getValue().asInstanceOf[Int])
    }
  })
  val value: Reactive[Int] = _value
  def setValue(value: Int) = model.setValue(value)

  //  private val _commits = EventSource[ActionEvent]
  private val editor = spinner.getEditor().getComponent(0).asInstanceOf[JFormattedTextField]
  //  editor.addActionListener(new ActionListener() {
  //    override def actionPerformed(event: ActionEvent) = {
  //      _commits << event
  //    }
  //  })
  //  val commits: Events[ActionEvent] = _commits

  editor.getFormatter().asInstanceOf[DefaultFormatter].setCommitsOnValidEdit(true);
}