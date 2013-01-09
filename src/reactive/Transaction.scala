package reactive

import java.util.UUID
import scala.collection.SortedSet

class Transaction {
  // use an arbitrary constant ordering to prevent deadlocks by lock acquisition during commits
  private var boxes = SortedSet[Var[_]]()(Ordering[Int].on[Var[_]] { _.hashCode() })
  private var values = Map[Var[_], Any]()
  
  def set[A](box: Var[A], value: A) = {
    boxes += box;
    values += (box -> value);
    this
  }
  
  def touch(box : Var[_]) = {
    boxes += box;
    values -= box;
  }

  def commit() {
    commitWhenAllLocked(boxes.toList, Map())
    reset()
  }
  
  private def commitWhenAllLocked(boxes: List[Var[_]], lastEvents: Map[UUID, UUID]): Event = {
    boxes match {
      case box :: tail =>
        box.lock.synchronized {
          setBoxFromMap(box, commitWhenAllLocked(tail, lastEvents + (box.uuid -> box.lastEvent))); 
        }
      case Nil =>
        new Event(lastEvents);
    }
  }
  
  private def setBoxFromMap[A](box : Var[A], event : Event) : Event = {
    box.set(values.get(box) match {
      case Some(value) => value.asInstanceOf[A] 
      case None => box.value;
    }, event);
  }
  
  private def reset() {
    values = values.empty
    boxes = boxes.empty
  }
}