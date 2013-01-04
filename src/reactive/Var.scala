package reactive
import java.util.UUID

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  val uuid = UUID.randomUUID();

  override lazy val dirty : Reactive[Boolean] = Var(false);
  
  def set(value : A) = {
    updateValue(Event(this), value);
  }
//  override val level = 0;
  override val sourceDependencies = Iterable[UUID](this.uuid)
}

object Var {
  def apply[A](name : String, value : A) : Var[A] = new Var(name, value)
  def apply[A](value : A) : Var[A] = apply("AnonVar", value)
}