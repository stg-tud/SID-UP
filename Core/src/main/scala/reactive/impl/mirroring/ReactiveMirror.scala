package reactive
package impl
package mirroring

trait ReactiveMirror[O, V, P, R <: Reactive[O, V, P, R]] {
  def mirror(initialValue: V, initialSourceDependencies: Reactive.Topology): R  
}