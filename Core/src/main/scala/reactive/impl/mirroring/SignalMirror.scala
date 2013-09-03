package reactive
package impl
package mirroring

import reactive.signals.Signal

class SignalMirror[A] extends ReactiveMirror[A, A, A, Signal[A]] {
  def mirror(initialValue: A, initialSourceDependencies: Reactive.Topology) = new MirroredSignal[A](initialValue, initialSourceDependencies)
}