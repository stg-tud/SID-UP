package ui
import java.awt.EventQueue

object AWTThreadSafe {
  def apply(op: => Unit) {
    if (EventQueue.isDispatchThread()) {
      op
    } else {
      EventQueue.invokeLater(new Runnable() {
        override def run() {
          op
        }
      })
    }
  }
}
