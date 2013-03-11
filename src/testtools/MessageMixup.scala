package testtools

import scala.collection.mutable
import reactive.Reactive._
import reactive.Var
import reactive.Transaction
import reactive.Signal
import reactive.impl.SignalImpl
import scala.util.Random
import remote.RemoteReactiveDependant
import util.Multiset
import java.util.UUID

class MessageMixup[A](input: Signal[A]) extends SignalImpl[A]("NetworkMixer[" + input.name + "]", input.now) with RemoteReactiveDependant[A] {
  input.addDependant(this);

  val messages = mutable.MutableList[(Txn, Multiset[UUID], Option[A])]()
  override def notify(sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A])(t: Txn) {
    messages.synchronized {
      messages += ((t, sourceDependenciesDiff, maybeValue));
    }
  }

  def releaseQueue() {
    Random.shuffle(messages.synchronized {
      val release = messages.toList;
      messages.clear()
      release
    }).foreach {
      case (t: Txn, sourceDependenciesDiff: Multiset[UUID], maybeValue: Option[A]) =>
        notifyDependants(sourceDependenciesDiff, maybeValue)(t)
    }
  }
}
