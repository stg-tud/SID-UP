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
  private var boxes = SortedSet[ReactiveSource[_]]()(Ordering[Int].on[ReactiveSource[_]] { _.hashCode() })
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
//    println("committed "+event);
    event
  }

  private def commitWhenAllLocked(boxes: List[ReactiveSource[_]], lastEvents: Map[UUID, UUID]): Event = {
    boxes match {
      case box :: tail =>
        box.lock.synchronized {
          setBoxFromMap(box, commitWhenAllLocked(tail, lastEvents + (box.uuid -> box.lastEvent)));
        }
      case Nil =>
        new Event(lastEvents);
    }
  }

  private def setBoxFromMap[A](box: ReactiveSource[A], event: Event): Event = {
    box.lastEvent = event.uuid;
    values.get(box) match {
      case Some(value) => box.emit(event, Some(value.asInstanceOf[A]));
      case None => box.emit(event, None);
    }
    event
  }

  private def reset() {
    values = values.empty
    boxes = boxes.empty
  }
}