package sort

import org.scalatest.FunSuite
import reactive.signals.Var

class SortTest extends FunSuite {
  val sort = new Sort[Int]

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
    sortedList.foreach { s => print(s.now + ", ")}

    assert(expectedList.map(_.now) === sortedList.map(_.now))
  }

  test("ssort works") {
    val list = Var(List(Var(5), Var(2), Var(1), Var(4), Var(3)))
    var expectedList = List(Var(1), Var(2), Var(3), Var(4), Var(5))
    val sortedList = sort.ssort(list)

    assert(expectedList.map(_.now) === sortedList.now.map(_.now))

    list << List(Var(12), Var(43), Var(3), Var(26), Var(11))
    expectedList = List(Var(3), Var(11), Var(12), Var(26), Var(43))

    assert(expectedList.map(_.now) === sortedList.now.map(_.now))
  }
}
