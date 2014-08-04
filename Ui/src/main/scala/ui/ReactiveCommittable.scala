package ui

import java.awt.event.ActionEvent

import reactive.events.EventStream

trait ReactiveCommittable {
  def commits: EventStream[ActionEvent]
}
