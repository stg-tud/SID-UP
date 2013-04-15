//package reactive
//package signals.impl
//
//import Reactive._
//import reactive.events.EventStream
//import reactive.impl.RemoteReactiveDeimport reactive.signals.impl.SignalImpl
//pendantImpl
//import reactive.impl.SignalImpl
//
//class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A, it: Txn) extends SignalImpl[A]("fold(" + source.name + ")", initialValue) with RemoteReactiveDependantImpl[B] {
//  TransactionBuilder.retryUntilSuccessWithLocalTransactionIfNeeded(it) {
//    connect(_, source)
//  }
//
//  override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[B])(implicit t: Txn) {
//    if (maybeValue.isDefined) {
//      notifyDependants(sourceDependenciesDiff, maybeValue.map { op(this(), _) });
//    } else {
//      super.notifyDependants(sourceDependenciesDiff, None);
//    }
//  }
//}
//
//object FoldSignal {
//  def apply[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) = {
//
//  }
//}
