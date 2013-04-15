//package reactive
//package signals.impl
//
//import Reactive._
//import reactive.events.EventStream
//import reactive.impl.RemoteReactiveDeimport reactive.signals.impl.SignalImpl
//pendantImpl
//import reactive.impl.SignalImpl
//
//class HoldSignal[A](override val changes: EventStream[A], initialValue: A, t: Txn) extends SignalImpl[A]("hold(" + changes.name + ")", initialValue) with RemoteReactiveDependantImpl[A] {
//  if(t == null) {
//    TransactionBuilder.retryUntilSuccess { t => connect(t, changes); }
//  } else {
//    connect(t, changes);
//  }
//
//  override def notify(sourceDependencyDiff: Multiset[UUID], value: Option[A])(implicit t: Txn) {
//    notifyDependants(sourceDependencyDiff, value);
//  }
//}
