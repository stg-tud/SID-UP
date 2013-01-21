package reactive

import scala.collection.immutable.Map

trait EventStream[A] extends Reactive[A] {
  def hold(initialValue: A): Signal[A] = new HoldSignal(this, initialValue);
  def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  def merge[B >: A](streams: EventStream[_ <: B]*): EventStream[B] = new MergeStream((this +: streams): _*);
  override def ordered : EventStream[A]
}