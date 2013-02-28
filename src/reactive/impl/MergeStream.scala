package reactive.impl
import java.util.UUID
import scala.collection.mutable
import reactive.EventStream
import reactive.EventStreamDependant
import reactive.Transaction

class MergeStream[A](streams: EventStream[A]*) extends EventStreamImpl[A]("merge(" + streams.map { _.name }.mkString(", ") + ")") with EventStreamDependant[A] {
  streams.foreach { _.addDependant(this) }
  override def sourceDependencies = streams.foldLeft(Map[UUID, UUID]()) { (accu, dep) => accu ++ dep.sourceDependencies }

  private val pending = mutable.Map[Transaction, (Int, Boolean)]()

  override def notifyEvent(event: Transaction, maybeValue: Option[A]) {
    if (shouldEmit(event, maybeValue.isDefined)) {
      propagate(event, maybeValue)
    }
  }
  private def shouldEmit(event: Transaction, canEmit: Boolean): Boolean = {
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