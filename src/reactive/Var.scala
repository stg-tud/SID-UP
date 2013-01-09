package reactive
import java.util.UUID
import scala.collection.mutable
import scala.collection.mutable.SynchronizedMap

class Var[A](name: String, initialValue: A, initialEvent : Event) extends Reactive[A](name, initialValue, Set(new Event(Map()))) {
  val uuid = UUID.randomUUID();
  override lazy val dirty: Reactive[Boolean] = Var(false);

  protected[reactive] var lastEvent = knownEvents.head.uuid
  protected[reactive] val lock = new Object()

  private val transaction = new Transaction();
  def set(value: A) {
    transaction.set(this, value);
    transaction.commit();
  }

  protected[reactive] def set(value: A, event: Event) : Event = {
    lastEvent = event.uuid;
    updateValue(event, value);
    event
  }

  //  override val level = 0;
  override def sourceDependencies = Map(uuid -> lastEvent);
}

object Var {
  def apply[A](name: String, value: A): Var[A] = new Var(name, value, new Event(Map()))
  def apply[A](value: A): Var[A] = apply("AnonVar", value)
}