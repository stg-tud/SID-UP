package asdf

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  private var nextValue = value;
  override def newValue = nextValue;
  
  def set(value : A) = {
    nextValue = value;
    new Propagator().run(this);
  }
  val level = 0;
}

object Var {
  def apply[A](name : String = "AnonVar", value : A) = new Var(name, value) 
}