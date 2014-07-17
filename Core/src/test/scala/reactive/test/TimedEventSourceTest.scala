package reactive.test

import org.scalatest.FunSuite
import reactive.events.TimedEventSource

class TimedEventSourceTest extends FunSuite {
  test("simple sanity tests") {
    val timedSource = TimedEventSource[Int]()
    val log = timedSource.single.log
    timedSource.schedule(5, System.currentTimeMillis())
    timedSource.schedule(10, System.currentTimeMillis() + 1000L)
    Thread.sleep(10)
    assert(log.single.now === List(5))
  }
}
