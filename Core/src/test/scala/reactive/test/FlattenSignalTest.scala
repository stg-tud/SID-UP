package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Var
import reactive.signals.Val
import reactive.Reactive
import reactive.testtools.NotificationLog
import reactive.signals.Signal
import reactive.Lift.single._
import reactive.Transaction
import reactive.TransactionBuilder

class FlattenSignalTest extends FunSuite {
  test("inner value update works") {
    val inner = Var(123)
    val outer: Var[Signal[Int]] = Var(inner)
    val flattened = outer.single.flatten
    val log = new NotificationLog(flattened)

    assertResult(123) { flattened.single.now }
    assertResult(Set(inner.uuid, outer.uuid)) { flattened.single.sourceDependencies }

    inner << 234
    assertResult(234) { flattened.single.now }
    assertResult(Set(inner.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification1 = log.dequeue()
    assertResult(true) { notification1.valueChanged }
    assertResult(234) { notification1.newValue }
    assertResult(false) { notification1.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, outer.uuid)) { notification1.newSourceDependencies }

    inner << 234
    assertResult(234) { flattened.single.now }
    assertResult(Set(inner.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification2 = log.dequeue()
    assertResult(false) { notification2.valueChanged }
    assertResult(234) { notification2.newValue }
    assertResult(false) { notification2.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, outer.uuid)) { notification2.newSourceDependencies }

    inner << 1
    assertResult(1) { flattened.single.now }
    assertResult(Set(inner.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification3 = log.dequeue()
    assertResult(true) { notification3.valueChanged }
    assertResult(1) { notification3.newValue }
    assertResult(false) { notification3.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, outer.uuid)) { notification3.newSourceDependencies }
  }

  test("outer value update works") {
    val inner1 = Var(123)
    val outer: Var[Signal[Int]] = Var(inner1)
    val flattened = outer.single.flatten
    val log = new NotificationLog(flattened)

    assertResult(123) { flattened.single.now }
    assertResult(Set(inner1.uuid, outer.uuid)) { flattened.single.sourceDependencies }

    val inner2 = Var(234)
    outer << inner2
    assertResult(234) { flattened.single.now }
    assertResult(Set(inner2.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification1 = log.dequeue()
    assertResult(true) { notification1.valueChanged }
    assertResult(234) { notification1.newValue }
    assertResult(true) { notification1.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }

    val inner3 = Var(234)
    outer << inner3
    assertResult(234) { flattened.single.now }
    assertResult(Set(inner3.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification2 = log.dequeue()
    assertResult(false) { notification2.valueChanged }
    assertResult(234) { notification2.newValue }
    assertResult(true) { notification2.sourceDependenciesChanged }
    assertResult(Set(inner3.uuid, outer.uuid)) { notification2.newSourceDependencies }

    inner1 << 1
    inner2 << 2
    inner3 << 444

    assertResult(444) { flattened.single.now }
    assertResult(Set(inner3.uuid, outer.uuid)) { flattened.single.sourceDependencies }
    assertResult(1) { log.size }
    val notification3 = log.dequeue()
    assertResult(true) { notification3.valueChanged }
    assertResult(444) { notification3.newValue }
    assertResult(false) { notification3.sourceDependenciesChanged }
    assertResult(Set(inner3.uuid, outer.uuid)) { notification3.newSourceDependencies }
  }
  
  test("double nested") {
    val inner = Var(1)
    val middle : Var[Signal[Int]] = Var(inner)
    val outer1 : Var[Signal[Signal[Int]]] = Var(middle)
    val flattened1 = outer1.single.flatten.single.flatten
    val log1 = new NotificationLog(flattened1)
    val outer2 : Var[Signal[Int]] = Var(middle.single.flatten)
    val flattened2 = outer2.single.flatten
    val log2 = new NotificationLog(flattened2)
    
    assertResult(1) { flattened1.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1) { flattened2.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }

    inner << 123

    assertResult(123) { flattened1.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1) { log1.size }
    val notification1 = log1.dequeue()
    assertResult(true) { notification1.valueChanged }
    assertResult(123) { notification1.newValue }
    assertResult(false) { notification1.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { notification1.newSourceDependencies }
    
    assertResult(123) { flattened2.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }
    assertResult(1) { log2.size }
    val notification2 = log2.dequeue()
    assertResult(true) { notification2.valueChanged }
    assertResult(123) { notification2.newValue }
    assertResult(false) { notification2.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { notification2.newSourceDependencies }
    
    middle << 234

    assertResult(234) { flattened1.single.now }
    assertResult(Set(middle.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1) { log1.size }
    val notification3 = log1.dequeue()
    assertResult(true) { notification3.valueChanged }
    assertResult(234) { notification3.newValue }
    assertResult(true) { notification3.sourceDependenciesChanged }
    assertResult(Set(middle.uuid, outer1.uuid)) { notification3.newSourceDependencies }
    
    assertResult(234) { flattened2.single.now }
    assertResult(Set(middle.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }
    assertResult(1) { log2.size }
    val notification4 = log2.dequeue()
    assertResult(true) { notification4.valueChanged }
    assertResult(234) { notification4.newValue }
    assertResult(true) { notification4.sourceDependenciesChanged }
    assertResult(Set(middle.uuid, outer2.uuid)) { notification4.newSourceDependencies }
    
    inner << 234
    
    assertResult(0) { log1.size }
    assertResult(0) { log2.size }
    
    middle << inner
    
    assertResult(234) { flattened1.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1, log1) { log1.size }
    val notification5 = log1.dequeue()
    assertResult(false) { notification5.valueChanged }
    assertResult(234) { notification5.newValue }
    assertResult(true) { notification5.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { notification5.newSourceDependencies }
    
    assertResult(234) { flattened2.single.now }
    assertResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }
    assertResult(1) { log2.size }
    val notification6 = log2.dequeue()
    assertResult(false) { notification6.valueChanged }
    assertResult(234) { notification6.newValue }
    assertResult(true) { notification6.sourceDependenciesChanged }
    assertResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { notification6.newSourceDependencies }
    
    val inner2 = Var(0)
    val middle2 = Var(inner2)
    
    outer1 << middle2
    outer2 << middle2.single.flatten
    
    assertResult(0) { flattened1.single.now }
    assertResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1) { log1.size }
    val notification7 = log1.dequeue()
    assertResult(true) { notification7.valueChanged }
    assertResult(0) { notification7.newValue }
    assertResult(true) { notification7.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { notification7.newSourceDependencies }
    
    assertResult(0) { flattened2.single.now }
    assertResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }
    assertResult(1) { log2.size }
    val notification8 = log2.dequeue()
    assertResult(true) { notification8.valueChanged }
    assertResult(0) { notification8.newValue }
    assertResult(true) { notification8.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { notification8.newSourceDependencies }
    
    inner2 << 999

    assertResult(999) { flattened1.single.now }
    assertResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { flattened1.single.sourceDependencies }
    assertResult(1) { log1.size }
    val notification9 = log1.dequeue()
    assertResult(true) { notification9.valueChanged }
    assertResult(999) { notification9.newValue }
    assertResult(false) { notification9.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { notification9.newSourceDependencies }
    
    assertResult(999) { flattened2.single.now }
    assertResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { flattened2.single.sourceDependencies }
    assertResult(1) { log2.size }
    val notification10 = log2.dequeue()
    assertResult(true) { notification10.valueChanged }
    assertResult(999) { notification10.newValue }
    assertResult(false) { notification10.sourceDependenciesChanged }
    assertResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { notification10.newSourceDependencies }
  }
}
