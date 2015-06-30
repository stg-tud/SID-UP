package reactive.sort

import reactive.lifting.Lift
import reactive.signals.{Signal, Val}

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

trait ROrdering[T] {
  def compare(a: T, b: T): Signal[Int]
}

class RSort[T](implicit ordering: ROrdering[T]) {
  def rmin(x: T, y: T): Signal[T] = {
    ordering.compare(x, y).map { comp => if (comp < 0) x else y }
  }

  def rmax(x: T, y: T): Signal[T] = {
    ordering.compare(x, y).map { comp => if (comp > 0) x else y }
  }

  def rrmin(x: Signal[T], y: Signal[T]): Signal[T] = {
    Lift.signal2(rmin)(x, y).flatten
  }

  def rrmax(x: Signal[T], y: Signal[T]): Signal[T] = {
    Lift.signal2(rmax)(x, y).flatten
  }

  def rsort1(list: List[T]): List[Signal[T]] = {
    rssort1(list.map(Val(_)))
  }


  def rsort2(list: List[T]): Signal[List[T]] = {
    import Signal._
    rsort1(list).signalTranspose
  }

  def rssort1(list: List[Signal[T]]): List[Signal[T]] = {
    // Bring the minimum element to the front of the list
    def bubbleUp(list: List[Signal[T]]): List[Signal[T]] = list.tail.foldLeft(List(list.head)) { (l, x) =>
      rrmin(l.head, x) :: rrmax(l.head, x) :: l.tail
    }

    list.size match {
      case 0 => list
      case 1 => list
      case _ => val l = bubbleUp(list); l.head :: rssort1(l.tail)
    }
  }

  def rssort2(list: List[Signal[T]]): Signal[List[T]] = {
    rssort1(list).signalTranspose
  }

  def rrsort(signal: Signal[List[T]]): Signal[List[T]] = {
    signal.map(rsort1).transposeS
  }
}
