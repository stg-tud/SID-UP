package example
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
import java.util.UUID
import reactive.Event
import util.SerializationSafe
import reactive.ReactiveDependant

object ResourceAllocationExample extends App {
  makeClient(new ServerFactory {
    override def connectToServer(requests: Reactive[Int]) = {
      fakeNetwork(makeServer(fakeNetwork(requests)))
    }
  })

  def fakeNetwork[A: SerializationSafe](input: Reactive[A]) = new Reactive[A]("NetworkDelayed[" + input.name + "]", input.value) with ReactiveDependant {
    override lazy val dirty: Reactive[Boolean] = Var(false);
    override def sourceDependencies = input.sourceDependencies 
    override def notifyUpdate(event: Event, valueChanged: Boolean) {
      val value = input.value
      spawn {
        Thread.sleep(500)
        updateValue(event, value)
      }
    }
  }

  def newFrame(title: String) = {
    val frame = new JFrame(title);
    frame.setLayout(new GridLayout(0, 2));
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame
  }

  def showFrame(frame: JFrame, sidewaysMoveFactor: Int) {
    frame.pack();
    frame.setLocationRelativeTo(null);
    val centered = frame.getLocation();
    frame.setLocation(centered.getX().toInt + sidewaysMoveFactor * frame.getWidth() / 2, centered.getY().toInt)
    frame.setVisible(true);
  }

  def makeServer(clientRequests: Reactive[Int]): Reactive[Int] = {
    val resourcesInput = new ReactiveSpinner(44)
    val committed = Signal("committed", clientRequests, resourcesInput.value) {
      math.min(clientRequests, resourcesInput.value);
    }

    val frame = newFrame("Server");

    frame.add(new JLabel("Available resources:"));
    frame.add(resourcesInput.asComponent);

    frame.add(new JLabel("Client requests:"));
    frame.add(new ReactiveLabel(clientRequests).asComponent)

    frame.add(new JLabel("Committed resources:"));
    frame.add(new ReactiveLabel(committed).asComponent)

    showFrame(frame, -1);

    committed
  }

  trait ServerFactory {
    def connectToServer(requests: Reactive[Int]): Reactive[Int]
  }
  def makeClient(serverFactory: ServerFactory) = {
    val requested = new ReactiveSpinner(4);
    val committedResources = serverFactory.connectToServer(requested.value);

    val frame = newFrame("Client");

    frame.add(new JLabel("Requested resources:"));
    frame.add(requested.asComponent)

    frame.add(new JLabel("Committed resources:"));
    frame.add(new ReactiveLabel(committedResources).asComponent)

    frame.add(new JLabel("Resource deficit:"));
    frame.add(new ReactiveLabel(Signal("deficit", requested.value, committedResources) {
      math.max(0, requested.value - committedResources);
    }).asComponent)

    showFrame(frame, 1);
  }
}