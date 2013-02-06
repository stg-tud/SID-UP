package example
import reactive.Signal
import javax.swing.JFrame
import testtools.FakeNetwork
import java.awt.GridLayout
import javax.swing.WindowConstants
import ui.ReactiveSpinner
import javax.swing.JLabel
import ui.ReactiveLabel
import reactive.Lift._
import reactive.LiftableWrappers._

object ResourceAllocationExample extends App {
  makeClient(new ServerFactory {
    override def connectToServer(requests: Signal[Int]) = {
      new FakeNetwork(makeServer(new FakeNetwork(requests)))
    }
  })

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

  def makeServer(clientRequests: Signal[Int]) = {
    val resourcesInput = new ReactiveSpinner(44)
    val committed = min(clientRequests, resourcesInput.value);

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
    def connectToServer(requests: Signal[Int]): Signal[Int]
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

    frame.add(new ReactiveLabel(substract(requested.value, committedResources)).asComponent)

    showFrame(frame, 1);
    frame
  }
}