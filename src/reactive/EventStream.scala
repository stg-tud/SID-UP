package reactive

import scala.collection.immutable.Map
import impl.FoldSignal
import reactive.impl.MergeStream
import reactive.impl.MappedEventStream
import reactive.impl.HoldSignal
import impl.FilteredEventStream
import scala.actors.threadpool.TimeoutException
import remote.RemoteEventStream

trait EventStream[+A] extends Reactive[A] with RemoteEventStream[A] {
  @throws(classOf[TimeoutException])
  def await(event: Event, timeout: Long = 0): Option[A]
  def hold[B >: A](initialValue: B): Signal[B]
  def map[B](op: A => B): EventStream[B]
  def merge[B >: A](streams: EventStream[B]*): EventStream[B]
  def fold[B](initialValue: B)(op: (B, A) => B): Signal[B]
  def filter(op: A => Boolean): EventStream[A]
}