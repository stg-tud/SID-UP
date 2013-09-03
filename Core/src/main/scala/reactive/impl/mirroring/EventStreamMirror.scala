package reactive
package impl
package mirroring

import reactive.events.EventStream

trait EventStreamMirror[A] extends ReactiveMirror[A, Unit, A, EventStream[A]] {
  def mirror(initialValue: Unit, initialSourceDependencies: Reactive.Topology) = new MirroredEventStream[A](initialSourceDependencies)
}