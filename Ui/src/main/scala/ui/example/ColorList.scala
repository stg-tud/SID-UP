package ui.example

import javax.swing.JFrame
import java.awt.BorderLayout
import reactive.lifting.Lift._
import ui.ReactiveLabel
import ui.ReactiveList
import javax.swing.WindowConstants
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JScrollPane

object ColorList extends App {
  // domain definitions
  class Color(override val toString: String, val actualColor: java.awt.Color)
  val Colors = List(
    new Color("black", java.awt.Color.BLACK),
    new Color("green", java.awt.Color.GREEN),
    new Color("red", java.awt.Color.RED),
    new Color("blue", java.awt.Color.BLUE),
    new Color("cyan", java.awt.Color.CYAN),
    new Color("magenta", java.awt.Color.MAGENTA),
    new Color("yellow", java.awt.Color.YELLOW),
    new Color("orange", java.awt.Color.ORANGE),
    new Color("pink", java.awt.Color.PINK),
    new Color("gray", java.awt.Color.GRAY),
    new Color("dark gray", java.awt.Color.DARK_GRAY),
    new Color("white", java.awt.Color.WHITE));

  // functionality
  val list = new ReactiveList[Color](Colors);
  val displayText = list.selectionOption.map(_.map(_.toString).getOrElse("none"));
  val displayColor = list.selectionOption.map(_.map(_.actualColor).getOrElse(java.awt.Color.BLACK))
  val label = new ReactiveLabel(displayText);
  label.foreground << displayColor;

  // window layout
  val window = new JFrame("Reactive ColorList");
  window.setLayout(new BorderLayout());
  window.add(new JLabel("Please select a color from this list:"), BorderLayout.NORTH);
  window.add(new JScrollPane(list.asComponent), BorderLayout.CENTER);
  val output = new Box(BoxLayout.X_AXIS)
  output.add(new JLabel("You have selected: "));
  output.add(label.asComponent)
  window.add(output, BorderLayout.SOUTH);
  // window configuration
  window.pack();
  window.setLocationRelativeTo(null);
  window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  window.setVisible(true);
}