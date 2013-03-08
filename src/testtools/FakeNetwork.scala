package testtools

import scala.concurrent.ops.spawn
import reactive.Signal
import reactive.Transaction
import reactive.impl.SignalImpl
import remote.RemoteReactiveDependant
import util.Multiset
import java.util.UUID
import dctm.vars.TransactionExecutionContext
import reactive.Reactive._

class FakeNetwork[A](input: Signal[A]) extends SignalImpl[A]("NetworkDelayed[" + input.name + "]", input.now) with RemoteReactiveDependant[A] {
  input.addDependant(this);
  override def notify(sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A])(implicit t: Txn) {
    spawn {
      Thread.sleep(500)
      notifyDependants(sourceDependenciesDiff, maybeValue);
    }
  }
}