package test
import reactive.EventSource
import testtools.ReactiveLog

object FoldTest extends App {
  val in = EventSource[Int]
  val out = in.fold(List.empty[Int]) { (list, value) => value :: list }
  val log = new ReactiveLog(out);

  in << 1
  in << 5
  in << 10
  in << 10
  val lastEvent = in << 2

  val result = out.awaitValue(lastEvent);
  log.assert(Nil, List(1), List(5, 1), List(10, 5, 1), List(10, 10, 5, 1), List(2, 10, 10, 5, 1))
  ReactiveLog.assert(result, List(2, 10, 10, 5, 1));
}