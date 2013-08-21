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
import java.awt.Color
import javax.swing.JComponent
import projections.reactives._
import reactive.events.EventStream
import reactive.events.EventSource
import javax.swing.DefaultListModel
import java.util.Date
import javax.swing.JList
import ui.ReactiveCheckbox
import javax.swing.ScrollPaneConstants

object ProjectionsUI {

  val sleeptime = 500

  def main(args: Array[String]): Unit = {
    if (args.length == 0) makeUIwithReactives()
    else args(0) match {
      case "rmi" => makeUIwithRMIObservers()
      case "sockets" => makeUIwithSocketObservers
      case _ => makeUIwithReactives()
    }
  }

  def makeUIwithSocketObservers() = {
    import projections.observer.sockets._
    import projections.observer.Message

    val orderVar = Var(Seq[Order]())
    val sales = Var(0)
    val purchases = Var(0)
    val management = Var(0)

    val c = new Client() {
      override def notifyObservers(v: Seq[Order]): Unit = {
        orderVar << v
        super.notifyObservers(v)
      }
    }
    val s = new Sales(sleeptime) {
      override def notifyObservers(v: Message[Int]): Unit = {
        sales << v.value
        super.notifyObservers(v)
      } 
    }
    val p = new Purchases(5) {
      override def notifyObservers(v: Message[Int]): Unit = {
        purchases << v.value
        super.notifyObservers(v)
      } 
    }
    val m = new Management() {
      override def notifyObservers(v: Int): Unit = {
        management << v
        super.notifyObservers(v)
      } 
    }

    c.init()
    p.init()
    s.init()
    m.init()

    makeUI(
      orders = orderVar,
      sales = sales,
      purchases = purchases,
      management = management
    ).observe{order => 
      future {c.makeOrder(order)}
    }
  }

  def makeUIwithRMIObservers() = {
    import projections.observer.rmi._
    import projections.observer.Message

    try {java.rmi.registry.LocateRegistry.createRegistry(1099)}
    catch {case _: Exception => println("registry already initialised")}

    val orderVar = Var(Seq[Order]())
    val sales = Var(0)
    val purchases = Var(0)
    val management = Var(0)

    val c = new Client() {
      override def notifyObservers(v: Seq[Order]): Unit = {
        orderVar << v
        super.notifyObservers(v)
      }
    }
    val s = new Sales(sleeptime) {
      override def notifyObservers(v: Message[Int]): Unit = {
        sales << v.value
        super.notifyObservers(v)
      } 
    }
    val p = new Purchases(5) {
      override def notifyObservers(v: Message[Int]): Unit = {
        purchases << v.value
        super.notifyObservers(v)
      } 
    }
    val m = new Management() {
      override def notifyObservers(v: Int): Unit = {
        management << v
        super.notifyObservers(v)
      } 
    }

    c.init()
    p.init()
    s.init()
    m.init()

    makeUI(
      orders = orderVar,
      sales = sales,
      purchases = purchases,
      management = management
    ).observe{order => 
      future {c.makeOrder(order)}
    }
  }

  def makeUIwithReactives() = {
    import projections.reactives._

    val makeOrder = EventSource[Order]()
    val c = new Client(makeOrder)
    val s = new Sales(sleeptime)
    val p = new Purchases(Var(5))
    val m = new Management()

    c.init()
    p.init()
    s.init()

    makeUI(
      orders = c.currentOrders,
      sales = s.total,
      purchases = p.total,
      management = m.difference
    ).observe{order => 
      future {makeOrder << order}
    }
  }

  def makeUI(
    orders: Signal[Seq[Order]],
    sales: Signal[Int],
    purchases: Signal[Int],
    management: Signal[Int]
  ): EventStream[Order] = {

    val orderSpinner = new ReactiveSpinner(10)
    val clientButton = new ReactiveButton("New Order")

    val orderStream = orderSpinner.value.pulse(clientButton.commits).map{Order(_)}

    val model = new DefaultListModel[Date]()
    val managPanic = management.map{_ < 0}.changes.filter(x => x).observe{ _ =>
      model.addElement(new Date())
    }

    val managementStatus = new JList(model)

    val managementDifference = new ReactiveLabel(management.map{d => f"Profit: $d%4d   "})

//    managementDifference.foreground << managPanic.map {
//      case false => Color.GREEN.darker
//      case true => Color.RED
//    }

    val checkBox = new ReactiveCheckbox("Glitch-Free")
    
    makeWindow("Management", 0, -200)(
      new JLabel("Management Status ") -> BorderLayout.NORTH,
      new JScrollPane(managementStatus, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) -> BorderLayout.SOUTH,
      managementDifference.asComponent -> BorderLayout.CENTER
    )

    makeWindow("Client", 0, +100)(
      new ReactiveLabel(
        orders.map{"Current orders: "+_.map{_.value}}
      ).asComponent -> BorderLayout.NORTH,
      clientButton.asComponent -> BorderLayout.SOUTH,
      orderSpinner.asComponent -> BorderLayout.EAST
    )

    makeWindow("Purchases", -100, 0)(
      new JLabel("Purchases Status ") -> BorderLayout.NORTH,
      new ReactiveLabel(purchases.map{t => f"total: $t%5d   "}).asComponent -> BorderLayout.SOUTH
    )

    makeWindow("Sales", +100, 0)(
      new JLabel("Sales Status ") -> BorderLayout.NORTH,
      new ReactiveLabel(sales.map{t => f"total: $t%5d   "}).asComponent -> BorderLayout.SOUTH
    )

    orderStream
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
  
  def transpose(dx : Int, dy : Int, window : Window) {
    val location = window.getLocation()
    window.setLocation(location.x + dx, location.y + dy)
  }
}
