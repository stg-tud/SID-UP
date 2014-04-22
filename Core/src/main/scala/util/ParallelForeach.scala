package util

import com.typesafe.scalalogging.slf4j.StrictLogging
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContext}
import java.util.concurrent.Executors
import scala.util.Try
import scala.util.Failure

object ParallelForeach extends StrictLogging {

  private val pool = Executors.newCachedThreadPool()

  private implicit val myExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = {
      pool.submit(runnable)
    }

    def reportFailure(t: Throwable): Unit = {
      t.printStackTrace()
    }
  }

  def parallelForeach[A, B](elements: Iterable[A])(op: A => B) = {
    if (elements.isEmpty) {
      Nil
    }
    else {
      val iterator = elements.iterator
      val head = iterator.next()

      val futures = iterator.foldLeft(List[(A, Future[B])]()) { (futures, element) =>
        (element -> Future { op(element) }) :: futures
      }
      val headResult = Try { op(head) }
      val results = headResult :: futures.map {
        case (element, future) =>
          logger.trace(s"$this join $element")
          Await.ready(future, Duration.Inf)
          future.value.get
      }

      logger.trace(s"$this fork/join completed")
      // TODO this should probably be converted into an exception thrown forward to
      // the original caller and be accumulated through all fork/joins along the path?
      results.foreach {
        case Failure(e) => e.printStackTrace()
        case _ =>
      }
      results
    }
  }
}

