package db

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
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
    val all: Signal[Set[Person]] = table.select { p => true}
    assert(all.now === Set(karl, katie, alice, bob))
  }

  test("select a specific family") {
    val familyY: Signal[Set[Person]] = table.select { person => person.lastName === "Y"}
    assert(familyY.now === Set(katie, alice))

    bob.lastName << "Y"
    assert(familyY.now === Set(katie, alice, bob))

    katie.lastName << "Z"
    assert(familyY.now === Set(alice, bob))
  }
}
