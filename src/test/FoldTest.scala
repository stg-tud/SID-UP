package test
import reactive.EventSource
import testtools.ReactiveLog

object FoldTest extends App {
  val in = EventSource[Int]
  val out = in.fold(List.empty[Int]){case (list, value) => value :: list }
  val log = new ReactiveLog(out);
  
  in << 1
  in << 5
  in << 10
  in << 10
  val lastEvent = in << 2
  val result = out.awaitValue(lastEvent);
  ReactiveLog.assert(result, List(1, 5, 10, 10, 2).reverse);
}