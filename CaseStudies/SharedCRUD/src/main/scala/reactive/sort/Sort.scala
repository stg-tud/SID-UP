package reactive.sort

import reactive.lifting.Lift
import reactive.signals.{Var, Signal}

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
    // Bring the minimum element to the front of the list
    def bubbleUp(list: List[Signal[T]]): List[Signal[T]] = list.tail.foldLeft(List(list.head)) { (l, x) =>
      rrmin(l.head, x) :: rrmax(l.head, x) :: l.tail
    }

    val signalList = list.map(Var(_))
    signalList.size match {
      case 0 => signalList
      case 1 => signalList
      case _ => val l = bubbleUp(signalList); l.head :: rsort1(l.tail.map(_.now))
    }
  }

  def rsort2(list: List[T]): Signal[List[T]] = {
    import Signal._
    rsort1(list).signalTranspose
  }

  def rrsort(signal: Signal[List[T]]): Signal[List[T]] = {
    signal.map(rsort1).transposeS
  }
}
