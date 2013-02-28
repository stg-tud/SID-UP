package testtools

import scala.concurrent.ops.spawn
import reactive.Signal
import reactive.Transaction
import reactive.impl.SignalImpl
import remote.RemoteReactiveDependant
import commit.CommitVote

class FakeNetwork[A](input: Signal[A]) extends SignalImpl[A]("NetworkDelayed[" + input.name + "]", input.now) with RemoteReactiveDependant[A] {
  input.addDependant(this);
  override def sourceDependencies = input.sourceDependencies
  override def prepareCommit(event: Transaction, commitVote: CommitVote, value: A) {
    spawn {
      Thread.sleep(500)
      prepareCommit(event, commitVote, { _ => value })
    }
  }
}