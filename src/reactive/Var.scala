package reactive
import scala.actors.threadpool.Executors
import scala.actors.threadpool.TimeUnit
import util.ThreadPool
import java.util.UUID

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  val uuid = UUID.randomUUID();

  def set(value : A) = {
    val pool = new ThreadPool(4);
    updateValue(pool, this.uuid, value);
    pool.awaitCompletion();
  }
  val level = 0;
  val sourceDependencies = Iterable[UUID](this.uuid)
}

object Var {
  def apply[A](name : String = "AnonVar", value : A) = new Var(name, value) 
}