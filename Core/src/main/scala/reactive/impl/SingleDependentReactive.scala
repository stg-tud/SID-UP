package reactive
package impl

trait SingleDependentReactive extends MultiDependentReactive {
  self: ReactiveImpl[_, _] with DependentReactive[_] =>

  protected def dependency: Reactive[_, _]

  override protected def dependencies: Set[Reactive[_, _]] = Set(dependency)
}
