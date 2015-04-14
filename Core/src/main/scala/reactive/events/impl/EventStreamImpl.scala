package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import reactive.signals.impl.FoldSignal
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamException
import reactive.remote.RemoteDependency
import reactive.remote.impl.RemoteEventStreamSubscriber
import reactive.remote.impl.RemoteEventStreamPublisher

@SerialVersionUID(1321321321L)
trait EventStreamImpl[A] extends ReactiveImpl[A, A] with EventStream[A] with Serializable {
  override def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue) { (_, value) => value }
  override def map[B](op: A => B): EventStream[B] = new TransformEventStream[B, A](this, _.map(op));
  override def collect[B](op: PartialFunction[A, B]): EventStream[B] = new TransformEventStream[B, A](this, _.collect(op));
  override def deOption[B](implicit evidence: A <:< Option[B]): EventStream[B] = new TransformEventStream[B, A](this, _.flatten)
  override def mapOption[B](op: A => Option[B]): EventStream[B] = new TransformEventStream[B, A](this, _.flatMap(op));
  override def filter(op: A => Boolean): EventStream[A] = new TransformEventStream[A, A](this, _.filter(op));
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue
  
  private lazy val remote = new RemoteEventStreamPublisher(this)
  @throws(classOf[ObjectStreamException])
  protected def writeReplace(): Any = EventStreamImpl.AutoRemoteEventStream(remote)
}

object EventStreamImpl {
  case class AutoRemoteEventStream[A](wrapped: RemoteDependency[A]) {
    @throws(classOf[ObjectStreamException])
    def readResolve(): Any = {
      new RemoteEventStreamSubscriber(wrapped)
    }
  }
}