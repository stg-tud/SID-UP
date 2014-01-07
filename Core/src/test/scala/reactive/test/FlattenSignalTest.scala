package reactive.test

import org.scalatest.FunSuite
import reactive.signals.Var
import reactive.signals.Val
import reactive.Reactive
import reactive.testtools.NotificationLog
import reactive.signals.Signal
import reactive.Lift._
import reactive.Transaction
import reactive.TransactionBuilder

class FlattenSignalTest extends FunSuite {
  test("inner value update works") {
    val inner = Var(123)
    val outer: Var[Signal[Int]] = Var(inner)
    val flattened = outer.flatten;
    val log = new NotificationLog(flattened)

    expectResult(123) { flattened.now }
    expectResult(Set(inner.uuid, outer.uuid)) { flattened.sourceDependencies(null) }

    inner << 234;
    expectResult(234) { flattened.now }
    expectResult(Set(inner.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification1 = log.dequeue
    expectResult(true) { notification1.valueChanged }
    expectResult(234) { notification1.newValue }
    expectResult(false) { notification1.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, outer.uuid)) { notification1.newSourceDependencies }

    inner << 234;
    expectResult(234) { flattened.now }
    expectResult(Set(inner.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification2 = log.dequeue
    expectResult(false) { notification2.valueChanged }
    expectResult(234) { notification2.newValue }
    expectResult(false) { notification2.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, outer.uuid)) { notification2.newSourceDependencies }

    inner << 1;
    expectResult(1) { flattened.now }
    expectResult(Set(inner.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification3 = log.dequeue
    expectResult(true) { notification3.valueChanged }
    expectResult(1) { notification3.newValue }
    expectResult(false) { notification3.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, outer.uuid)) { notification3.newSourceDependencies }
  }

  test("outer value update works") {
    val inner1 = Var(123)
    val outer: Var[Signal[Int]] = Var(inner1)
    val flattened = outer.flatten;
    val log = new NotificationLog(flattened)

    expectResult(123) { flattened.now }
    expectResult(Set(inner1.uuid, outer.uuid)) { flattened.sourceDependencies(null) }

    val inner2 = Var(234)
    outer << inner2
    expectResult(234) { flattened.now }
    expectResult(Set(inner2.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification1 = log.dequeue
    expectResult(true) { notification1.valueChanged }
    expectResult(234) { notification1.newValue }
    expectResult(true) { notification1.sourceDependenciesChanged }
    expectResult(Set(inner2.uuid, outer.uuid)) { notification1.newSourceDependencies }

    val inner3 = Var(234)
    outer << inner3
    expectResult(234) { flattened.now }
    expectResult(Set(inner3.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification2 = log.dequeue
    expectResult(false) { notification2.valueChanged }
    expectResult(234) { notification2.newValue }
    expectResult(true) { notification2.sourceDependenciesChanged }
    expectResult(Set(inner3.uuid, outer.uuid)) { notification2.newSourceDependencies }

    inner1 << 1
    inner2 << 2
    inner3 << 444

    expectResult(444) { flattened.now }
    expectResult(Set(inner3.uuid, outer.uuid)) { flattened.sourceDependencies(null) }
    expectResult(1) { log.size }
    val notification3 = log.dequeue
    expectResult(true) { notification3.valueChanged }
    expectResult(444) { notification3.newValue }
    expectResult(false) { notification3.sourceDependenciesChanged }
    expectResult(Set(inner3.uuid, outer.uuid)) { notification3.newSourceDependencies }
  }
  
  test("double nested") {
    val inner = Var(1)
    val middle : Var[Signal[Int]] = Var(inner)
    val outer1 : Var[Signal[Signal[Int]]] = Var(middle)
    val flattened1 = outer1.flatten.flatten
    val log1 = new NotificationLog(flattened1)
    val outer2 : Var[Signal[Int]] = Var(middle.flatten)
    val flattened2 = outer2.flatten
    val log2 = new NotificationLog(flattened2)
    
    expectResult(1) { flattened1.now }
    expectResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1) { flattened2.now }
    expectResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }

    inner << 123;
    
    expectResult(123) { flattened1.now }
    expectResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1) { log1.size }
    val notification1 = log1.dequeue
    expectResult(true) { notification1.valueChanged }
    expectResult(123) { notification1.newValue }
    expectResult(false) { notification1.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { notification1.newSourceDependencies }
    
    expectResult(123) { flattened2.now }
    expectResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }
    expectResult(1) { log2.size }
    val notification2 = log2.dequeue
    expectResult(true) { notification2.valueChanged }
    expectResult(123) { notification2.newValue }
    expectResult(false) { notification2.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { notification2.newSourceDependencies }
    
    middle << 234;
        
    expectResult(234) { flattened1.now }
    expectResult(Set(middle.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1) { log1.size }
    val notification3 = log1.dequeue
    expectResult(true) { notification3.valueChanged }
    expectResult(234) { notification3.newValue }
    expectResult(true) { notification3.sourceDependenciesChanged }
    expectResult(Set(middle.uuid, outer1.uuid)) { notification3.newSourceDependencies }
    
    expectResult(234) { flattened2.now }
    expectResult(Set(middle.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }
    expectResult(1) { log2.size }
    val notification4 = log2.dequeue
    expectResult(true) { notification4.valueChanged }
    expectResult(234) { notification4.newValue }
    expectResult(true) { notification4.sourceDependenciesChanged }
    expectResult(Set(middle.uuid, outer2.uuid)) { notification4.newSourceDependencies }
    
    inner << 234
    
    expectResult(0) { log1.size }
    expectResult(0) { log2.size }
    
    middle << inner
    
    expectResult(234) { flattened1.now }
    expectResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1, log1) { log1.size }
    val notification5 = log1.dequeue
    expectResult(false) { notification5.valueChanged }
    expectResult(234) { notification5.newValue }
    expectResult(true) { notification5.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, middle.uuid, outer1.uuid)) { notification5.newSourceDependencies }
    
    expectResult(234) { flattened2.now }
    expectResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }
    expectResult(1) { log2.size }
    val notification6 = log2.dequeue
    expectResult(false) { notification6.valueChanged }
    expectResult(234) { notification6.newValue }
    expectResult(true) { notification6.sourceDependenciesChanged }
    expectResult(Set(inner.uuid, middle.uuid, outer2.uuid)) { notification6.newSourceDependencies }
    
    val inner2 = Var(0)
    val middle2 = Var(inner2)
    
    outer1 << middle2
    outer2 << middle2.flatten
    
    expectResult(0) { flattened1.now }
    expectResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1) { log1.size }
    val notification7 = log1.dequeue
    expectResult(true) { notification7.valueChanged }
    expectResult(0) { notification7.newValue }
    expectResult(true) { notification7.sourceDependenciesChanged }
    expectResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { notification7.newSourceDependencies }
    
    expectResult(0) { flattened2.now }
    expectResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }
    expectResult(1) { log2.size }
    val notification8 = log2.dequeue
    expectResult(true) { notification8.valueChanged }
    expectResult(0) { notification8.newValue }
    expectResult(true) { notification8.sourceDependenciesChanged }
    expectResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { notification8.newSourceDependencies }
    
    inner2 << 999;
    
    expectResult(999) { flattened1.now }
    expectResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { flattened1.sourceDependencies(null) }
    expectResult(1) { log1.size }
    val notification9 = log1.dequeue
    expectResult(true) { notification9.valueChanged }
    expectResult(999) { notification9.newValue }
    expectResult(false) { notification9.sourceDependenciesChanged }
    expectResult(Set(inner2.uuid, middle2.uuid, outer1.uuid)) { notification9.newSourceDependencies }
    
    expectResult(999) { flattened2.now }
    expectResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { flattened2.sourceDependencies(null) }
    expectResult(1) { log2.size }
    val notification10 = log2.dequeue
    expectResult(true) { notification10.valueChanged }
    expectResult(999) { notification10.newValue }
    expectResult(false) { notification10.sourceDependenciesChanged }
    expectResult(Set(inner2.uuid, middle2.uuid, outer2.uuid)) { notification10.newSourceDependencies }   
  }
}