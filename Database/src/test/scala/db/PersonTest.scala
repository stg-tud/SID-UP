package db

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import reactive.events.EventSource
import reactive.signals.{Signal, Var}

class PersonTest extends FunSuite {

  class Person(val firstName: Var[String], val lastName: Var[String])

  object Person {
    def apply(firstName: String, lastName: String) = {
      new Person(Var(firstName), Var(lastName))
    }
  }

  val karl = Person("Karl", "X")
  val katie = Person("Katie", "Y")
  val alice = Person("Alice", "Y")
  val bob = Person("Bob", "Z")
  var table = Table[Person](karl, katie, alice, bob)

  test("select all") {
    val all: Signal[Iterable[Person]] = table.select { p => Var(true)}
    assert(all.now === Set(karl, katie, alice, bob))
  }

  test("select a specific family") {
    val familyY: Signal[Iterable[Person]] = table.select { person => person.lastName.map {_ == "Y"}}
    assert(familyY.now === Set(katie, alice))

    bob.lastName << "Y"
    assert(familyY.now === Set(katie, alice, bob))

    katie.lastName << "Z"
    assert(familyY.now === Set(alice, bob))
  }

  test("insert new person") {
    val max = Person("Max", "Z")

    val all = table.select { p => Var(true)}

    assert(!all.now.exists { _ == max})
    table.insert(max)
    assert(all.now.exists { _ == max})
  }

  test("remove a person") {
    val all = table.select { p => Var(true)}

    assert(all.now.exists { _ == bob})
    table.remove(bob, katie)
    assert(!all.now.exists { _ == bob})
    assert(!all.now.exists { _ == katie})
  }
}
