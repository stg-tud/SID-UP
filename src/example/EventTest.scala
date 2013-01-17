package example
import javax.swing.JFrame
import java.awt.GridLayout
import ui.ReactiveCommitable
import ui.ReactiveComponent
import ui.ReactiveLabel
import javax.swing.JComponent
import javax.swing.JPanel
import ui.ReactiveButton
import reactive.Var
import java.awt.Color
import javax.swing.WindowConstants

object EventTest extends App {
	val frame = new JFrame("Eventtest");
	val content = new JPanel(new GridLayout(0,1));
	frame.setContentPane(content);
	object ReactiveFrame extends {
	  override val asComponent = content;
	} with ReactiveComponent {
	  override val observeWhileVisible = List()
	}
	
	val button = new ReactiveButton(Var("                        Change Font Color                        "));
	val label = new ReactiveLabel(ReactiveFrame.mousePosition);
	button.commits.map{event => new Color(math.random.toFloat, math.random.toFloat, math.random.toFloat) }.observe {color => 
	  label.asComponent.setForeground(color);
	}
	frame.add(label.asComponent);
	frame.add(button.asComponent);
	
	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
}