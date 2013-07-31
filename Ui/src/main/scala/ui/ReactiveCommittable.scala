package ui

import java.awt.event.ActionEvent
import reactive.events.EventStream
import javax.swing.JComponent

trait ReactiveCommittable {
  val commits: EventStream[ActionEvent]
}