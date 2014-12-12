package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Var
import reactive.testtools.NotificationLog

class VarTest extends FunSuite {
  test("var << x works") {
    val v = Var(1)
    assertResult(Set(v.uuid)) { v.sourceDependencies }
    assertResult(1) { v.now }

    val log = new NotificationLog(v)
    assertResult(true) { log.isEmpty }

    v << 2

    assertResult(2) { v.now }
    assertResult(Set(v.uuid)) { v.sourceDependencies }
    assertResult(1) { log.size }
    val event1 = log.dequeue()
    assertResult(false) { event1.sourceDependenciesChanged }
    assertResult(Set(v.uuid)) { event1.newSourceDependencies }
    assertResult(true) { event1.valueChanged }
    assertResult(2) { event1.newValue }

    v << 3

    assertResult(3) { v.now }
    assertResult(Set(v.uuid)) { v.sourceDependencies }
    assertResult(1) { log.size }
    val event2 = log.dequeue()
    assertResult(false) { event2.sourceDependenciesChanged }
    assertResult(Set(v.uuid)) { event2.newSourceDependencies }
    assertResult(true) { event2.valueChanged }
    assertResult(3) { event2.newValue }

    v << 3

    assertResult(3) { v.now }
    assertResult(Set(v.uuid)) { v.sourceDependencies }
    assertResult(1) { log.size }
    val event3 = log.dequeue()
    assertResult(false) { event3.sourceDependenciesChanged }
    assertResult(Set(v.uuid)) { event3.newSourceDependencies }
    assertResult(false) { event3.valueChanged }
    assertResult(3) { event3.newValue }
  }
}
