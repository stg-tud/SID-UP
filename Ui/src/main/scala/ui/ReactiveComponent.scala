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
import reactive.events.EventStream
import reactive.events.EventSource

class ReactiveComponent[T <: JComponent](val asComponent: T) {
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

  lazy val mousePosition: Signal[Option[Point]] = {
    val _mousePosition = Var[Option[Point]](None)
    val adapter = new MouseAdapter() {
      override def mouseMoved(evt: MouseEvent) {
        _mousePosition << Some(evt.getPoint());
      }
      override def mouseExited(evt: MouseEvent) {
        _mousePosition << None;
      }
      override def mouseEntered(evt: MouseEvent) {
        mouseMoved(evt);
      }
    }
    asComponent.addMouseListener(adapter);
    asComponent.addMouseMotionListener(adapter);
    _mousePosition
  };

  lazy val mouseDowns: EventStream[Point] = {
    val source = EventSource[Point]
    val adapter = new MouseAdapter() {
      override def mousePressed(evt: MouseEvent) {
        source << evt.getPoint()
      }
    }
    asComponent.addMouseListener(adapter)
    source
  }

  lazy val mouseUps: EventStream[Point] = {
    val source = EventSource[Point]
    val adapter = new MouseAdapter() {
      override def mouseReleased(evt: MouseEvent) {
        source << evt.getPoint()
      }
    }
    asComponent.addMouseListener(adapter)
    source
  }

  lazy val mouseDrags: EventStream[(Point, Point)] = {
    val source = EventSource[(Point, Point)]
    val adapter = new MouseAdapter() {
    	var lastPressedPosition: Point = _;
      override def mousePressed(evt: MouseEvent) {
        lastPressedPosition = evt.getPoint()
      }
      override def mouseDragged(evt: MouseEvent) {
        source << (lastPressedPosition -> evt.getPoint());
        mousePressed(evt)
      }
    }
    asComponent.addMouseMotionListener(adapter)
    source
  }

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
