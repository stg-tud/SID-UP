package projections

import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.WindowConstants
import reactive.Lift._
import reactive.LiftableWrappers._
import reactive.signals._
import ui.ReactiveButton
import ui.ReactiveLabel
import ui.ReactiveList
import ui.ReactiveSpinner
import java.awt.Window
import scala.concurrent._
import ExecutionContext.Implicits.global

object ProjectionsUI extends App {
	// domain definitions
//	val week = Var(1)
//	SignalRegistry.register("week", week)

	// client
//	val purchasesSpinner = new ReactiveSpinner(5)
	val salesSpinner = new ReactiveSpinner(10)
	val clientButton = new ReactiveButton("New Order")
	val currentOrder = salesSpinner.value.map(new Order[Int](_))

	val client = new Client[Int]("client1", salesSpinner.value.pulse(clientButton.commits).map{new Order(_)})
	client.startWorking()

	val purch = new Purchases[Int]()
	val sales = new Sales[Int]()
	val manag = new Management[Int]()

	sales.startWorking()
	purch.startWorking()
	manag.startWorking()

	// functionality
	// management
	val managementStatus = new ReactiveLabel(manag.panic.map{
		case true => "panicking"
		case false => "normal"
		});
	val managementDifference = new ReactiveLabel(manag.difference.map{d => s" (difference $d)    "})


	// management layout
	{
		val window = new JFrame("Management")
		window.setLayout(new BorderLayout())
		val output = new Box(BoxLayout.X_AXIS)
		output.add(new JLabel("Management Status is: "))
		output.add(managementStatus.asComponent)
		output.add(managementDifference.asComponent)
		window.add(output, BorderLayout.CENTER);
		// window configuration
		window.pack();
		window.setLocationRelativeTo(null);
		transpose(0, -100, window);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setVisible(true);
	}

	// client layout
	{
		val window = new JFrame("Client");
		window.setLayout(new BorderLayout());
		window.add(new JLabel("Make a new Order"), BorderLayout.NORTH)
		window.add(clientButton.asComponent, BorderLayout.SOUTH)
//		window.add(purchasesSpinner.asComponent, BorderLayout.WEST)
		window.add(salesSpinner.asComponent, BorderLayout.EAST)

		// window configuration
		window.pack();
		window.setLocationRelativeTo(null);
		transpose(0, +100, window);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setVisible(true);
	}

	// purchases layout
	{
		val window = new JFrame("Purchases")
		window.setLayout(new BorderLayout())
		val output = new Box(BoxLayout.X_AXIS)
		output.add(new JLabel("Purchases Status is: "))
		output.add(new ReactiveLabel(purch.total.map{t => s"total: $t     "}).asComponent)
		window.add(output, BorderLayout.CENTER);
		window.add(new ReactiveLabel(purch.calculating.map{
			case true => "calculating"
			case false => "idle"
			}).asComponent, BorderLayout.SOUTH)
		// window configuration
		window.pack();
		window.setLocationRelativeTo(null);
		transpose(-100, 0, window);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setVisible(true);
	}

	// sales layout
	{
		val window = new JFrame("Sales")
		window.setLayout(new BorderLayout())
		val output = new Box(BoxLayout.X_AXIS)
		output.add(new JLabel("Sales Status is: "))
		output.add(new ReactiveLabel(sales.total.map{t => s"total: $t     "}).asComponent)
		window.add(output, BorderLayout.CENTER);
		window.add(new ReactiveLabel(sales.calculating.map{
			case true => "calculating"
			case false => "idle"
			}).asComponent, BorderLayout.SOUTH)
		// window configuration
		window.pack();
		transpose(200, 0, window);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setVisible(true);
	}
	
	def transpose(dx : Int, dy : Int, window : Window) {
	  val location = window.getLocation();
	  window.setLocation(location.x + dx, location.y + dy);
	}
}
