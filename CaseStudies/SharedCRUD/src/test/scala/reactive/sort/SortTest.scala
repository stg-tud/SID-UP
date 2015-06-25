package reactive.sort

import org.scalatest.FunSuite
import reactive.lifting.Lift
import reactive.signals.Var
import reactive.signals.Signal

class SortTest extends FunSuite {
  class SignalPairOrdering extends ROrdering[(Signal[Int], Signal[Int])] {
    override def compare(a: (Signal[Int], Signal[Int]), b: (Signal[Int], Signal[Int])): Signal[Int] = {
      val asum = Lift.signal2 {(x:Int, y:Int) => x+y } (a._1, a._2)
      val bsum = Lift.signal2 {(x:Int, y:Int) => x+y } (b._1, b._2)

      Lift.signal2(Ordering[Int].compare)(asum, bsum)
    }
  }
  implicit val pairOrdering = new SignalPairOrdering

  val sort = new Sort[Int]
  val rsort = new RSort[(Signal[Int], Signal[Int])]

  test("min works") {
    val x: Var[Int] = Var(1)
    val y: Var[Int] = Var(2)
    val min = sort.min(x, y)

    // x = 1, y = 2
    assert(1 === min.now)

    // x = 1, y = 0
    y << 0
    assert(0 === min.now)

    // x = 1, y = 1
    y << 1
    assert(1 === min.now)
  }

  test("max works") {
    val x: Var[Int] = Var(1)
    val y: Var[Int] = Var(2)
    val max = sort.max(x, y)

    // x = 1, y = 2
    assert(2 === max.now)

    // x = 3, y = 2
    x << 3
    assert(3 === max.now)

    // x = 2, y = 2
    x << 2
    assert(2 === max.now)
  }

  test("sort works") {
    val list = List(Var(5), Var(2), Var(1), Var(4), Var(3))
    val expectedList = List(Var(1), Var(2), Var(3), Var(4), Var(5))
    val sortedList = sort.sort(list)

    assert(expectedList.map(_.now) === sortedList.map(_.now))
  }

  test("ssort works") {
    
    val a = Var(2)
    val b = Var(4)
    val c = Var(6)
    val d = Var(8)
    val e = Var(10)
    
    val list: Var[List[Signal[Int]]] = Var(List(e, d, c, b, a))
    val sorted = sort.ssort(list)
    def expect() = {
      assert(list.now.map(_.now).sorted === sorted.now.map(_.now))
    }
    
    expect()
    
    a << 100
    expect()
    
    c << 4
    expect()
    
    list << List(a, b, c, d)
    expect()
    
    list << List(a.map(_ * 2), b, c, e.map(5 - _))
    expect()
    
    e << 0
    expect()
  }

  test("ROrdering works") {
    val a = (Var(1), Var(2))
    val b = (Var(3), Var(4))
    val ordering = new SignalPairOrdering()
    assert(ordering.compare(a, b).now === -1)

    a._1 << 6
    assert(ordering.compare(a, b).now === 1)

    b._2 << 5
    assert(ordering.compare(a, b).now === 0)
  }

  test("rmin works") {
    val x = (Var(1), Var(2))
    val y = (Var(3), Var(4))
    val min = rsort.rmin(x, y)

    // x = (1, 2), y = (3, 4)
    assert((1, 2) === (min.now._1.now, min.now._2.now))

    // x = (1, 2), y = (0, 1)
    y._1 << 0
    y._2 << 1
    assert((0, 1) === (min.now._1.now, min.now._2.now))

    // x = (0, 1), y = (0, 1)
    x._1 << 0
    x._2 << 1
    assert((0, 1) === (min.now._1.now, min.now._2.now))
  }

  test("rmax works") {
    val x = (Var(1), Var(2))
    val y = (Var(3), Var(4))
    val max = rsort.rmax(x, y)

    // x = (1, 2), y = (3, 4)
    assert((3, 4) === (max.now._1.now, max.now._2.now))

    // x = (1, 2), y = (0, 1)
    y._1 << 0
    y._2 << 1
    assert((1, 2) === (max.now._1.now, max.now._2.now))

    // x = (0, 1), y = (0, 1)
    x._1 << 0
    x._2 << 1
    assert((0, 1) === (max.now._1.now, max.now._2.now))
  }

  test("rsort1 works") {
    implicit val ord = new Ordering[(Int, Int)] {
      override def compare(x: (Int, Int), y: (Int, Int)): Int = Ordering[Int].compare(x._1+x._2, y._1+y._2)
    }

    val a = (Var(1), Var(2))
    val b = (Var(3), Var(4))
    val c = (Var(5), Var(6))
    val d = (Var(7), Var(8))
    val e = (Var(9), Var(10))

    val list: List[(Signal[Int], Signal[Int])] = List(e, d, c, b, a)
    val sorted = rsort.rsort1(list)
    def expect() = {
      assert(list.map(s => (s._1.now, s._2.now)).sorted === sorted.map(s => (s.now._1.now, s.now._2.now)))
    }

    expect()

    a._1 << 5
    a._2 << 3
    expect()
//
//    c << 4
//    expect()
//
//    list << List(a, b, c, d)
//    expect()
//
//    list << List(a.map(_ * 2), b, c, e.map(5 - _))
//    expect()
//
//    e << 0
//    expect()
  }
}
