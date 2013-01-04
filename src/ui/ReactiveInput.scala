package ui
import javax.swing.JComponent
import reactive.Reactive

trait ReactiveInput[A] extends ReactiveComponent {
	val value : Reactive[A]
	def setValue(value : A)
}