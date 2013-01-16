package ui
import javax.swing.JComponent
import reactive.Reactive
import javax.swing.event.AncestorListener
import javax.swing.event.AncestorEvent

trait ReactiveComponent {
  def asComponent: JComponent

  protected case class ReactiveAndObserverPair[A](reactive: Reactive[A], op: A => Unit) {
    def activate {
      reactive.observe(op);
      op(reactive.value);
    }
    def deactivate {
      reactive.unobserve(op);
    }
  }

  protected def observeInEDT[A](reactive: Reactive[A])(op: A => Unit) = {
    new ReactiveAndObserverPair(reactive, { value: A => AWTThreadSafe(op(value)) });
  }

  protected val observeWhileVisible: List[ReactiveAndObserverPair[_]]

  asComponent.addAncestorListener(new AncestorListener() {
    override def ancestorAdded(event: AncestorEvent) {
      observeWhileVisible.foreach { _.activate }
    }
    override def ancestorRemoved(event: AncestorEvent) {
      observeWhileVisible.foreach { _.deactivate }
    }
    override def ancestorMoved(event: AncestorEvent) {}
  });
}