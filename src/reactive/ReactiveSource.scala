package reactive
import java.util.UUID

trait ReactiveSource[A] {
  self: Reactive[A] =>

  private val transaction = new Transaction();
  protected def emit(value: A) = {
    transaction.set(this, value);
    transaction.commit();
  }

  val uuid = UUID.randomUUID();
  protected[reactive] var lastEvent = UUID.randomUUID();
  protected[reactive] val lock = new Object()

  protected[reactive] def emit(event: Event, maybeValue : Option[A])

  override def sourceDependencies = Map(uuid -> lastEvent);
}