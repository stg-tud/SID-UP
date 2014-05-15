package stm.concurrency.test

import scala.concurrent.ExecutionContext
import scala.concurrent.stm._
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.ThreadPoolExecutor
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.TimeUnit
import java.util.concurrent.SynchronousQueue

object UnsynchronizedTransformTest extends App {
  val pool = ExecutionContext.fromExecutorService(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new SynchronousQueue[Runnable]()))
  val v1 = Ref(5)
  def wrapInInvocationTargetException[A](op: => A) {
    try {
      op
    } catch {
      case e: Throwable => throw new InvocationTargetException(e)
    }
  }
//  def go() = {
    println(v1.single.get)
    atomic { tx =>
      val f1 = Future { wrapInInvocationTargetException(v1.transform(v => v + 1)(tx)) }(pool)
      val f2 = Future { wrapInInvocationTargetException(v1.transform(v => v + 1)(tx)) }(pool)
      Await.ready(f1, Duration(5, SECONDS))
      Await.ready(f2, Duration(5, SECONDS))
    }
    println(v1.single.get)
//  }
}