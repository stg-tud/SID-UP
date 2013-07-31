package ui
import javax.swing.JComponent
import reactive.Reactive
import javax.swing.event.AncestorListener
import javax.swing.event.AncestorEvent
import reactive.signals.Signal
import java.awt.Point
import reactive.signals.Var
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import reactive.signals.Var
import java.awt.event.MouseAdapter
import reactive.signals.RoutableVar
import reactive.Lift.valueToSignal

class ReactiveComponent[T <: JComponent](val asComponent : T) {
  protected def observeInEDT[A](reactive: Signal[A])(op: A => Unit) = {
    new ReactiveComponent.ReactiveAndObserverPair(reactive, { value: A => AWTThreadSafe(op(value)) }).activate;
  }

  lazy val foreground = {
    val routableVar = RoutableVar(asComponent.getForeground());
    observeInEDT(routableVar) { asComponent.setForeground(_) }
    routableVar
  }
  lazy val background = {
    val routableVar = RoutableVar(asComponent.getBackground());
    observeInEDT(routableVar) { asComponent.setBackground(_) }
    routableVar
  }
  lazy val enabled = {
    val routableVar = RoutableVar(asComponent.isEnabled());
    observeInEDT(routableVar) { asComponent.setEnabled(_) }
    routableVar
  }

  lazy val _mousePosition : Var[Option[Point]] = {
    val adapter = new MouseAdapter() {
      override def mouseMoved(evt : MouseEvent) {
        _mousePosition << Some(evt.getPoint());
      }
      override def mouseExited(evt : MouseEvent) {
        _mousePosition << None;
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

object ReactiveComponent {
    private case class ReactiveAndObserverPair[A](reactive: Signal[A], op: A => Unit) {
    def activate() {
      reactive.observe(op);
      op(reactive.now);
    }
    def deactivate() {
      reactive.unobserve(op);
    }
  }
}
