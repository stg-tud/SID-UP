package reactive
import java.util.UUID
import scala.collection.mutable

class MergeStream[A](streams: EventStream[_ <: A]*) extends EventStreamImpl[A]("merge(" + streams.map { _.name }.mkString(", ") + ")") with ReactiveDependant[A] {
  streams.foreach { _.addDependant(this) }
  override def sourceDependencies = streams.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }

  private val pending = mutable.Map[Event, (Int, Boolean)]()
  override def notifyEvent(event: Event) {
   if(shouldEmit(event, false)) {notifyDependants(event)}
  }
  override def notifyUpdate(event: Event, value: A) {
    if(shouldEmit(event, true)) {notifyDependants(event, value)};
  }
  private def shouldEmit(event: Event, canEmit: Boolean): Boolean = {
    pending.synchronized {
      def shouldEmit(pendingNotifications: Int, hasEmitted: Boolean, canEmit: Boolean): Boolean = {
        if (pendingNotifications == 0) {
          pending -= event;
          !hasEmitted
        } else {
          val willEmit = !hasEmitted && canEmit
          pending += (event -> ((pendingNotifications, hasEmitted || willEmit)));
          willEmit
        }
      }
      pending.get(event) match {
        case Some((pendingNotifications, hasEmitted)) =>
          shouldEmit(pendingNotifications - 1, hasEmitted, canEmit);
        case None =>
          val pendingNotifications = streams.filter { stream => !(stream.sourceDependencies.keySet & event.sourcesAndPredecessors.keySet).isEmpty }.length
          shouldEmit(pendingNotifications - 1, false, canEmit);
      }
    }
  }
}