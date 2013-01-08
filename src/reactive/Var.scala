package reactive
import java.util.UUID

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  val uuid = UUID.randomUUID();
  private var lastEvent : UUID = null;
  override lazy val dirty : Reactive[Boolean] = Var(false);
  
  private val lock = new Object()
  def set(value : A) = {
    updateValue(newEvent, value);
  }
  
  private def newEvent = {
    lock.synchronized{
      val event = new Event(uuid, lastEvent);
      lastEvent = event.uuid;
      event
    }
  }
//  override val level = 0;
  override val sourceDependencies = Iterable[UUID](this.uuid)
}

object Var {
  def apply[A](name : String, value : A) : Var[A] = new Var(name, value)
  def apply[A](value : A) : Var[A] = apply("AnonVar", value)
}