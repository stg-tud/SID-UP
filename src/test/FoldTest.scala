package test
import reactive.EventSource
import testtools.Asserts

object FoldTest extends App {
  val in = EventSource[Int]
  val out = in.fold(List.empty[Int]) { (list, value) => value :: list }
  val log = out.log

  in << 1
  in << 5
  in << 10
  in << 10
  val lastEvent = in << 2

  val result = out.await(lastEvent);
  Asserts.assert(List(Nil, List(1), List(5, 1), List(10, 5, 1), List(10, 10, 5, 1), List(2, 10, 10, 5, 1)), log.now)
  Asserts.assert(List(2, 10, 10, 5, 1), result);
}