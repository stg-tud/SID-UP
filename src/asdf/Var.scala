package asdf

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  def set(value : A) = {
    dirty
    newValueAndClean(value)
  }
  val level = 0;
}

object Var {
  def apply[A](name : String = "AnonVar", value : A) = new Var(name, value) 
}