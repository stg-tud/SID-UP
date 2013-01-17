package ui

import java.awt.event.ActionEvent
import reactive.EventStream

trait ReactiveCommitable extends ReactiveComponent {
	def commits : EventStream[ActionEvent]
}