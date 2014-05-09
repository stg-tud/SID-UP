package reactive.test
import reactive.events.EventSource
import org.scalatest.FunSuite

class FoldTest extends FunSuite {
  test("fold works") {
    val in = EventSource[Int]
    val out = in.single.fold(List.empty[Int]) { (list, value) => value :: list }
    val log = out.single.log

    in << 1
    in << 5
    in << 10
    in << 10
    val lastEvent = in << 2

    Thread.sleep(10);
    val result = out.single.now;
    assertResult(List(Nil, List(1), List(5, 1), List(10, 5, 1), List(10, 10, 5, 1), List(2, 10, 10, 5, 1))) { log.single.now }
    assertResult(List(2, 10, 10, 5, 1)) { result }
  }
}
