package reactive
package events
package impl

import reactive.impl.ReactiveImpl
import reactive.signals.Signal
import reactive.signals.impl.FoldSignal
import reactive.impl.mirroring.ReactiveMirror
import reactive.impl.mirroring.ReactiveNotificationDependant
import reactive.impl.mirroring.ReactiveNotification

trait EventStreamImpl[A] extends ReactiveImpl[A, Unit, A, EventStream[A]] with EventStream[A] {
  self =>
  override def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue) { (_, value) => value }
  override def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  override def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  override def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  override def log = fold(List[A]())((list, elem) => list :+ elem)
  override def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);

  protected override def getObserverValue(transaction: Transaction, pulseValue: A) = pulseValue

  def mirror = new ReactiveMirror[A, Unit, A, EventStream[A]] {
    def mirror = new EventStreamImpl[A] {
      private var sourceDependencies = self.sourceDependencies(null)
      def now = Unit
      def value(transaction: Transaction) = Unit
      def sourceDependencies(t: Transaction) = sourceDependencies
      self.addNotificationDependant(null, new ReactiveNotificationDependant[A] {
        def fire(notification: ReactiveNotification[A]) {
          notification.sourceDependencies.foreach { sourceDependencies = _ }
          doPulse(notification.transaction, notification.sourceDependencies.isDefined, notification.pulse)
        }
      })
    }
  }

}