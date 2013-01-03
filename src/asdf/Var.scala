package asdf
import scala.actors.threadpool.Executors
import scala.actors.threadpool.TimeUnit
import util.ThreadPool

class Var[A](name : String, currentValue : A) extends Reactive[A](name, currentValue) {
  
  def set(value : A) = {
    val pool = new ThreadPool(4);
    updateValue(pool, this, value);
    pool.awaitCompletion();
  }
  val level = 0;
  val sourceDependencies = Iterable[Var[_]](this)
}

object Var {
  def apply[A](name : String = "AnonVar", value : A) = new Var(name, value) 
}