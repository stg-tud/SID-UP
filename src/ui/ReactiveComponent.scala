package ui
import javax.swing.JComponent
import reactive.Reactive
import javax.swing.event.AncestorListener
import javax.swing.event.AncestorEvent
import reactive.Signal
import java.awt.Point
import reactive.Var
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import reactive.Var
import java.awt.event.MouseAdapter

trait ReactiveComponent {
  def asComponent: JComponent

  protected case class ReactiveAndObserverPair[A](reactive: Signal[A], op: A => Unit) {
    def activate {
      reactive.observe(op);
      op(reactive.now);
    }
    def deactivate {
      reactive.unobserve(op);
    }
  }

  protected def observeInEDT[A](reactive: Signal[A])(op: A => Unit) = {
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
  
  lazy val _mousePosition : Var[Option[Point]] = {
    val adapter = new MouseAdapter() {
      override def mouseMoved(evt : MouseEvent) {
        _mousePosition.set(Some(evt.getPoint()));
      }
      override def mouseExited(evt : MouseEvent) {
        _mousePosition.set(None);
      }
      override def mouseEntered(evt : MouseEvent) {
        mouseMoved(evt);
      }
    }
    asComponent.addMouseListener(adapter);
    asComponent.addMouseMotionListener(adapter);
    Var(None)
  };
  
  lazy val mousePosition : Signal[Option[Point]] = _mousePosition;
}