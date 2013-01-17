package ui
import javax.swing.JComponent
import reactive.Reactive
import reactive.Signal

trait ReactiveInput[A] extends ReactiveComponent {
	val value : Signal[A]
	def setValue(value : A)
}