package testtools

import scala.collection.mutable
import reactive.Reactive
import reactive.Var
import reactive.Transaction
import reactive.Signal
import reactive.impl.SignalImpl
import scala.util.Random
import remote.RemoteReactiveDependant
import util.Multiset
import commit.CommitVote
import java.util.UUID

class MessageMixup[A](input: Signal[A]) extends SignalImpl[A]("NetworkMixer[" + input.name + "]", input.now) with RemoteReactiveDependant[A] {
  input.addDependant(this);
  
  val messages = mutable.MutableList[(Transaction, CommitVote[Transaction], Multiset[UUID], Option[A])]()
  override def notify(transaction: Transaction, commitVote: CommitVote[Transaction], sourceDependenciesDiff : Multiset[UUID], maybeValue: Option[A]) {
    messages.synchronized {
      messages += ((transaction, commitVote, sourceDependenciesDiff, maybeValue));
    }
  }

  def releaseQueue() {
    Random.shuffle(messages.synchronized {
      val release = messages.toList;
      messages.clear()
      release
    }).foreach { { notifyDependants(_,_,_,_) }.tupled }
  }
}
