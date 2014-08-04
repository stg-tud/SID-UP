package projections

import java.awt.{BorderLayout, Window}
import java.util.Date
import javax.swing.{DefaultListModel, JComponent, JFrame, JLabel, JList, JScrollPane, ScrollPaneConstants, WindowConstants}

import reactive.Lift.single._
import reactive.signals._
import ui.{ReactiveButton, ReactiveCheckbox, ReactiveLabel, ReactiveSpinner}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object ProjectionsUI {

  val sleeptime = 500

  def main(args: Array[String]): Unit = {
    if (args.length == 0) makeUIwithReactives()
    else args(0) match {
      case "rmi" => makeUIwithRMIObservers()
      case _ => makeUIwithReactives()
    }
  }

  def makeUIwithRMIObservers() = {
    import projections.observer._

    try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
    catch { case _: Exception => println("registry already initialised") }

    val sales = Var(0)
    val purchases = Var(0)
    val management = Var(0)

    val c = new Client()
    val s = new Sales(sleeptime) {
      override def publish(v: Message): Unit = {
        sales << v.total
        super.publish(v)
      }
    }
    val p = new Purchases(5) {
      override def publish(v: Message): Unit = {
        purchases << v.total
        super.publish(v)
      }
    }
    val m = new Management() {
      override def publish(v: Int): Unit = {
        management << v
        super.publish(v)
      }
    }

    val (orders, glitch) = makeUI(
      sales = sales,
      purchases = purchases,
      management = management)
    orders.single.observe { order =>
      Future { c.setOrders(order) }
    }
    m.disableTransaction << glitch
  }

  def makeUIwithReactives() = {
    import projections.reactives._

    try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
    catch { case _: Exception => println("registry already initialised") }

    val setOrder = Var[Seq[Order]](Seq())
    val c = new Client(setOrder)
    val s = new Sales(sleeptime)
    val p = new Purchases(Var(5))
    val m = new Management()

    val (orders, glitch) = makeUI(
      sales = s.total,
      purchases = p.total,
      management = m.difference)
    orders.single.observe { order =>
      Future { setOrder << order }
    }
  }

  def makeUI(
    sales: Signal[Int],
    purchases: Signal[Int],
    management: Signal[Int]) = {

    val orderSpinner = new ReactiveSpinner(10)
    val clientButton = new ReactiveButton("New Order")

    val orderStream = orderSpinner.value.single.pulse(clientButton.commits).single.map { Order }
    val orders = orderStream.single.log

    val model = new DefaultListModel[String]()
    val managPanic = scala.concurrent.stm.atomic { implicit tx =>
      management.map { _ < 0 }.changes.filter(x => x).observe { _ =>
        model.addElement("Mail sent on " + new Date())
      }
    }

    val managementStatus = new JList(model)

    val managementDifference = new ReactiveLabel(management.single.map { d => f"Profit: $d%4d   " })

    //    managementDifference.foreground << managPanic.map {
    //      case false => Color.GREEN.darker
    //      case true => Color.RED
    //    }

    val checkBox = new ReactiveCheckbox("Deactivate Simulated Glitch Freedom")

    makeWindow("Management", 0, -200)(
      new JLabel("Management Status ") -> BorderLayout.NORTH,
      managementDifference.asComponent -> BorderLayout.CENTER,
      new JScrollPane(managementStatus, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) -> BorderLayout.SOUTH)

    makeWindow("Client", 0, +100)(
      new ReactiveLabel(
        orders.single.map { "Current orders: " + _.map { _.value } }).asComponent -> BorderLayout.NORTH,
      clientButton.asComponent -> BorderLayout.EAST,
      orderSpinner.asComponent -> BorderLayout.WEST,
      checkBox.asComponent -> BorderLayout.SOUTH)

    makeWindow("Purchases", -100, 0)(
      new JLabel("Purchases Status ") -> BorderLayout.NORTH,
      new ReactiveLabel(purchases.single.map { t => f"total: $t%5d   " }).asComponent -> BorderLayout.SOUTH)

    makeWindow("Sales", +100, 0)(
      new JLabel("Sales Status ") -> BorderLayout.NORTH,
      new ReactiveLabel(sales.single.map { t => f"total: $t%5d   " }).asComponent -> BorderLayout.SOUTH)

    (orders, checkBox.value)
  }

  def makeWindow(name: String, posx: Int, posy: Int)(components: Tuple2[JComponent, String]*) = {
    val window = new JFrame(name)
    window.setLayout(new BorderLayout())
    for ((com, dir) <- components) {
      window.add(com, dir)
    }
    // window configuration
    window.pack()
    window.setLocationRelativeTo(null)
    transpose(posx, posy, window)
    window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    window.setVisible(true)
    window
  }

  def transpose(dx: Int, dy: Int, window: Window): Unit = {
    val location = window.getLocation()
    window.setLocation(location.x + dx, location.y + dy)
  }
}
