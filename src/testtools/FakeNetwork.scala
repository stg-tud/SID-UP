package testtools

import scala.concurrent.ops.spawn
import reactive.Signal
import reactive.Transaction
import reactive.impl.SignalImpl
import remote.RemoteReactiveDependant
import dctm.commit.CommitVote
import util.Multiset
import java.util.UUID

class FakeNetwork[A](input: Signal[A]) extends SignalImpl[A]("NetworkDelayed[" + input.name + "]", input.now) with RemoteReactiveDependant[A] {
  input.addDependant(this);
  override def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A]) {
    spawn {
      Thread.sleep(500)
      notifyDependants(transaction, commitVote, sourceDependenciesDiff, maybeValue);
    }
  }
}