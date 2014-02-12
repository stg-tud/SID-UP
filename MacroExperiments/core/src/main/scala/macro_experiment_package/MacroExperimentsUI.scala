package macro_experiment_package

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
import ui.ReactiveTextField
import java.awt.Window
import scala.concurrent._
import ExecutionContext.Implicits.global
import java.awt.Color
import javax.swing.JComponent
//import projections.reactives._
import reactive.events.EventStream
import reactive.events.EventSource
import javax.swing.DefaultListModel
import java.util.Date
import javax.swing.JList
import ui.ReactiveCheckbox
import javax.swing.ScrollPaneConstants
import scala.util.control.Exception.allCatch
import reactive.remote.RemoteSignal
import macro_package.hello
//import macro_package._ //.Distributed

@hello
object MacroExperimentsUI {

  val sleeptime = 500

  def main(args: Array[String]): Unit = {

    javax.swing.JOptionPane.showMessageDialog(null, this.hello)

    try {
      java.rmi.registry.LocateRegistry.createRegistry(1099)
    } catch {
      case _: Exception => println("registry already initialised")
    }

    // rtfCelsius -toFloat-> celsius -netzwerk-> remoteCelsius
    //   -calculation-> fahrenheit -label-> rlFahrenheit
    val rtfCelsius = new ReactiveTextField

    val celsius: Signal[Float] = rtfCelsius.value.map {
      string => allCatch.opt(string.toFloat).getOrElse(0)
    }
    RemoteSignal.rebind("celsius", celsius)
    //@Distributed val celsius: Signal[Float] = rtfCelsius.value ... the same without rebind
    val remoteCelsius: Signal[Float] = RemoteSignal.lookup[Float]("celsius")
    val fahrenheit: Signal[Float] = remoteCelsius.map {value => value * 1.8f + 32.0f}
    val rlFahrenheit = new ReactiveLabel(fahrenheit.map {
      value => f"$value%5f"
    })

    makeWindow("Celsius", 20, 20)(
      new JLabel("Celsius") -> BorderLayout.NORTH,
      rtfCelsius.asComponent -> BorderLayout.SOUTH
    )
    makeWindow("Fahrenheit", 200, 200)(
      new JLabel("Fahrenheit") -> BorderLayout.NORTH,
      rlFahrenheit.asComponent -> BorderLayout.SOUTH
    )
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
    window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    window.setVisible(true)
    window
  }
}
