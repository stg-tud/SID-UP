package reactive.sort

import reactive.lifting.Lift
import reactive.signals.Signal
import reactive.signals.impl.FunctionalSignal
import reactive.Transaction
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
    def bubbleUp(list: List[Signal[T]]): List[Signal[T]] = list.tail.foldLeft(List(list.head)) { (l, x) =>
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

trait ROrdering[T]
abstract class RSort[T](implicit ordering: ROrdering[T]) {
  def rmin(x: T, y: T): Signal[T]
  def rrmin(x: Signal[T], y: Signal[T]): Signal[T] = {
    Lift.signal2(rmin)(x, y).flatten
  }
  def rsort1(list: List[T]): List[Signal[T]]
  def rsort2(list: List[T]): Signal[List[T]] = {
    import Signal._
    rsort1(list).signalTranspose
  }
  def rrsort(signal: Signal[List[T]]): Signal[List[T]] = {
    signal.map(rsort1).transposeS
  }
}
