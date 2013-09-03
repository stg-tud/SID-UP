package reactive
package impl
package mirroring

abstract class ReactiveNotificationDependantImpl[-P](reactive: Reactive[_, _, P, _]) extends Reactive.Dependant with ReactiveNotificationDependant[P] {
  reactive.addDependant(null, this);
  override def apply(transaction: Transaction, pulsed: Boolean, dependenciesChanged: Boolean) {
    fire(ReactiveNotification(transaction, reactive.pulse(transaction), if(dependenciesChanged) Some(reactive.sourceDependencies(transaction)) else None));
  }
}