package reactive.impl
import reactive.EventStream

trait EventStreamDefaults[A] extends EventStream[A] {
  override def hold[B >: A](initialValue: B) = new HoldSignal(this, initialValue);
  override def map[B](op: A => B) = new MappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[_ <: B]*) = new MergeStream((this +: streams): _*);
  override def fold[B](initialValue: B)(op: (B, A) => B) = new FoldSignal(initialValue, this, op);
}