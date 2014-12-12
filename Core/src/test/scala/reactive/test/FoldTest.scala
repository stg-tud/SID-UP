package reactive.test
import reactive.events.EventSource
import org.scalatest.FunSuite

class FoldTest extends FunSuite {
  test("fold works") {
    val in = EventSource[Int]()
    val out = in.fold(List.empty[Int]) { (list, value) => value :: list }
    val log = out.log

    in << 1
    in << 5
    in << 10
    in << 10
    val lastEvent = in << 2

    Thread.sleep(10)
    val result = out.now
    assertResult(List(Nil, List(1), List(5, 1), List(10, 5, 1), List(10, 10, 5, 1), List(2, 10, 10, 5, 1))) { log.now }
    assertResult(List(2, 10, 10, 5, 1)) { result }
  }
}
