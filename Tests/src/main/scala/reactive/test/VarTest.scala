package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Var
import reactive.testtools.NotificationLog

class VarTest extends FunSuite {
  test("var << x works") {
    val v = Var(1)
    expectResult(Set(v.uuid)) { v.sourceDependencies(null) }
    expectResult(1) { v.now }

    val log = new NotificationLog(v)
    expectResult(true) { log.isEmpty }

    v << 2;

    expectResult(2) { v.now }
    expectResult(Set(v.uuid)) { v.sourceDependencies(null) }
    expectResult(1) { log.size }
    val event1 = log.dequeue
    expectResult(false) { event1.sourceDependenciesChanged }
    expectResult(Set(v.uuid)) { event1.newSourceDependencies }
    expectResult(Some(2)) { event1.pulse }
    expectResult(2) { event1.value }

    v << 3;

    expectResult(3) { v.now }
    expectResult(Set(v.uuid)) { v.sourceDependencies(null) }
    expectResult(1) { log.size }
    val event2 = log.dequeue
    expectResult(false) { event2.sourceDependenciesChanged }
    expectResult(Set(v.uuid)) { event2.newSourceDependencies }
    expectResult(Some(3)) { event2.pulse }
    expectResult(3) { event2.value }

    v << 3;

    expectResult(3) { v.now }
    expectResult(Set(v.uuid)) { v.sourceDependencies(null) }
    expectResult(1) { log.size }
    val event3 = log.dequeue
    expectResult(false) { event3.sourceDependenciesChanged }
    expectResult(Set(v.uuid)) { event3.newSourceDependencies }
    expectResult(Some(3)) { event3.pulse }
    expectResult(3) { event3.value }
  }
}