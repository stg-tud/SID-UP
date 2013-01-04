package asdf
import scala.concurrent.ops.spawn
import javax.swing.JFrame
import reactive.Var
import java.awt.GridLayout
import javax.swing.JLabel
import ui.ReactiveSpinner
import ui.ReactiveLabel
import reactive.Signal
import reactive.Reactive
import javax.swing.WindowConstants
import reactive.DependantReactive
import java.util.UUID

object ResourceAllocationExample extends App {
  def newFrame(title: String) = {
    val frame = new JFrame(title);
    frame.setLayout(new GridLayout(0, 2));
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame
  }
  def showFrame(frame: JFrame) {
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  def makeServer(clientRequests: Reactive[Int]): Reactive[Int] = {
    val resourcesInput = new ReactiveSpinner(5)
    val committed = Signal(clientRequests, resourcesInput.value) {
      math.min(clientRequests, resourcesInput.value);
    }

    val frame = newFrame("Server");

    frame.add(new JLabel("Available resources:"));
    frame.add(resourcesInput.asComponent);

    frame.add(new JLabel("Client requests:"));
    frame.add(new ReactiveLabel(clientRequests).asComponent)

    frame.add(new JLabel("Committed resources:"));
    frame.add(new ReactiveLabel(committed).asComponent)

    showFrame(frame);

    committed
  }

  def fakeNetwork[A](input: Reactive[A]) = new DependantReactive[A]("NetworkDelay:" + input.name, input.value, input) {
    override def notifyUpdate(source: UUID, event: UUID, valueChanged: Boolean) {
      val value = input.value
      spawn {
        Thread.sleep(500)
        if (valueChanged) {
          updateValue(source, event, value)
        } else {
          notifyDependencies(source, event, valueChanged)
        }
      }
    }
  }

  val requested = new ReactiveSpinner(4);
  val committedResources = fakeNetwork(makeServer(fakeNetwork(requested.value)))

  val frame = newFrame("Client");

  frame.add(new JLabel("Requested resources:"));
  frame.add(requested.asComponent)

  frame.add(new JLabel("Committed resources:"));
  frame.add(new ReactiveLabel(committedResources).asComponent)

  frame.add(new JLabel("Resource deficit:"));
  frame.add(new ReactiveLabel(Signal(requested.value, committedResources) {
    math.max(0, requested.value - committedResources);
  }).asComponent)

  showFrame(frame);
}