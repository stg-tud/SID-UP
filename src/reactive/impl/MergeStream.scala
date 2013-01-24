package reactive.impl
import java.util.UUID
import scala.collection.mutable
import reactive.EventStream
import reactive.ReactiveDependant
import reactive.Event

class MergeStream[A](streams: EventStream[A]*) extends StatelessEventStreamImpl[A]("merge(" + streams.map { _.name }.mkString(", ") + ")") with ReactiveDependant[A] {
  streams.foreach { _.addDependant(this) }
  override def sourceDependencies = streams.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }

  private val pending = mutable.Map[Event, (Int, Boolean)]()

  override def notifyEvent(event: Event, maybeValue: Option[A]) {
    if (shouldEmit(event, maybeValue.isDefined)) {
      propagate(event, maybeValue)
    }
  }
  private def shouldEmit(event: Event, canEmit: Boolean): Boolean = {
    pending.synchronized {
      val (pendingNotifications: Int, hasEmitted: Boolean) = pending.get(event).getOrElse((streams.count { _.isConnectedTo(event) }, false))
      if (pendingNotifications == 1) {
        pending -= event;
        !hasEmitted
      } else {
        pending += (event -> ((pendingNotifications - 1, hasEmitted || canEmit)));
        !hasEmitted && canEmit
      }
    }
  }
}