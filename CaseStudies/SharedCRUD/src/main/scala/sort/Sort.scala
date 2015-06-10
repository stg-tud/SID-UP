package sort

import reactive.lifting.Lift
import reactive.signals.Signal

class Sort[T](implicit ordering: Ordering[T]) {
  def min(x: Signal[T], y: Signal[T]): Signal[T] = {
    Lift.signal2(ordering.compare)(x, y).flatMap {
      comp => if (comp < 0) x else y
    }
  }

  def max(x: Signal[T], y: Signal[T]): Signal[T] = {
    Lift.signal2(ordering.compare)(x, y).flatMap {
      comp => if (comp > 0) x else y
    }
  }

  def sort(list: List[Signal[T]]): List[Signal[T]] = {
    // Bring the minimum element to the front of the list
    def bubbleUp(list: List[Signal[T]]): List[Signal[T]] = list.tail.foldLeft(List(list.head)) {(l, x) =>
        min(l.head, x) :: max(l.head, x) :: l.tail
    }

    list.size match {
      case 0 => list
      case 1 => list
      case _ => val l = bubbleUp(list); l.head :: sort(l.tail)
    }
  }

  def ssort(list: Signal[List[Signal[T]]]): Signal[List[Signal[T]]] = list.map(sort)
}
