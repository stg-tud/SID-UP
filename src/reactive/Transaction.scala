package reactive

import java.util.UUID
import scala.collection.SortedSet

/**
 *  Note: This is only called a transaction due to it's similar usage pattern
 *  and for a lack of a better word. It does create guarantee happened-before
 *  relations between all elements of two only partially overlapping
 *  transactions. It only causes multiple set-instructions to produce a
 *  singular observable event. The updated values of two synchronously
 *  executing transactions can still mix arbitrarily.
 */
class Transaction {
  // use an arbitrary constant ordering to prevent deadlocks by lock acquisition during commits
  private var boxes = SortedSet[ReactiveSource[_]]()(new Ordering[ReactiveSource[_]] {
    override def compare(a: ReactiveSource[_], b: ReactiveSource[_]) = a.uuid.compareTo(b.uuid)
  })
  private var values = Map[ReactiveSource[_], Any]()

  def set[A](box: ReactiveSource[A], value: A) = {
    boxes += box;
    values += (box -> value);
    this
  }

  def touch(box: ReactiveSource[_]) {
    boxes += box;
    values -= box;
  }

  def commit() = {
    val event = commitWhenAllLocked(boxes.toList, Map())
    reset()
    event
  }

  private def commitWhenAllLocked(boxes: List[ReactiveSource[_]], lastEvents: Map[UUID, UUID]): Event = {
    boxes match {
      case box :: tail =>
        box.lock.synchronized {
          setBoxFromMap(box, commitWhenAllLocked(tail, lastEvents + (box.uuid -> box.lastEventId)));
        }
      case Nil =>
        new Event(lastEvents);
    }
  }

  private def setBoxFromMap[A](box: ReactiveSource[A], event: Event): Event = {
    box.lastEventId = event.uuid;
    box.emit(event, values.get(box).asInstanceOf[Option[A]])
    event
  }

  private def reset() {
    values = values.empty
    boxes = boxes.empty
  }
}