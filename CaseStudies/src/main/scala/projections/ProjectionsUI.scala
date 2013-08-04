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

	// client
	// val purchasesSpinner = new ReactiveSpinner(5)
	val orderSpinner = new ReactiveSpinner(10)
	val clientButton = new ReactiveButton("New Order")
	val currentOrder = orderSpinner.value.map(new Order[Int](_))

	val client = new Client[Int]("client1", orderSpinner.value.pulse(clientButton.commits).map{new Order(_)})
	val purch = new Purchases[Int](Var(5))
	val sales = new Sales[Int]()
	val manag = new Management[Int]()

	client.startWorking()
	sales.startWorking()
	purch.startWorking()
	manag.startWorking()

	// functionality
	// management
	val managementStatus = new ReactiveLabel(manag.panic.map{
		case true => "panicking"
		case false => "normal"
		});
	val managementDifference = new ReactiveLabel(manag.difference.map{d => f"difference $d%4d   "})


	// management layout
	{
		val window = new JFrame("Management")
		window.setLayout(new BorderLayout())
		window.add(new JLabel("Management Status "), BorderLayout.NORTH)
		window.add(managementStatus.asComponent, BorderLayout.EAST)
		window.add(managementDifference.asComponent, BorderLayout.WEST)
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
		window.add(orderSpinner.asComponent, BorderLayout.EAST)

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
		window.add(new JLabel("Purchases Status "),BorderLayout.NORTH)
		window.add(new ReactiveLabel(purch.total.map{t => f"total: $t%5d   "}).asComponent, BorderLayout.WEST)
		window.add(new ReactiveLabel(purch.calculating.map{
			case true => "calculating"
			case false => "idle"
			}).asComponent, BorderLayout.EAST)
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
		window.add(new JLabel("Sales Status "),BorderLayout.NORTH)
		window.add(new ReactiveLabel(sales.total.map{t => f"total: $t%5d   "}).asComponent, BorderLayout.WEST)
		window.add(new ReactiveLabel(sales.calculating.map{
			case true => "calculating"
			case false => "idle"
			}).asComponent, BorderLayout.EAST)
		// window configuration
		window.pack();
		window.setLocationRelativeTo(null);
		transpose(100, 0, window);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		window.setVisible(true);
	}
	
	def transpose(dx : Int, dy : Int, window : Window) {
	  val location = window.getLocation();
	  window.setLocation(location.x + dx, location.y + dy);
	}
}
