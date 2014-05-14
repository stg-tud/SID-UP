package stm.concurrency.test

import scala.concurrent._
import scala.concurrent.stm._
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import java.util.concurrent.Executors
import scala.util.Failure
import java.lang.reflect.InvocationTargetException
import scala.util.Success
import java.util.concurrent.TimeoutException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.SynchronousQueue

object MultiThreadedStmTransactionTest extends App {
  val initial = 5
  val numberConcurrentTransactions = 4
  val numberConcurrentThreadsPerTransaction = 4
  val updatesPerThreadPerTransaction = 11111
  val expected = initial + numberConcurrentThreadsPerTransaction * numberConcurrentTransactions * updatesPerThreadPerTransaction
  val timeoutDuration = Duration.Inf //Duration(1000000, MILLISECONDS)

  val pool = ExecutionContext.fromExecutorService(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new SynchronousQueue[Runnable]()))

  val count = Ref(initial)
  (for (h <- 1 to numberConcurrentTransactions) yield {

    Future[Unit] {
      try {

        atomic { tx =>
          val lock = tx// new Object
          val inTxnThreads = for (i <- 1 to numberConcurrentThreadsPerTransaction) yield {
            val threadId = h + "-" + i
            threadId -> Future[Unit] {
              Thread.currentThread().setName(Thread.currentThread.getName() + " @ " + threadId)
//              println("Starting " + threadId)
              try {
                for (j <- 1 to updatesPerThreadPerTransaction) yield {
//                  println(threadId + " entering")
                  lock.synchronized {
//                    println(threadId + " entered")
                    count.transform { old =>
//                      println(threadId + "-" + j)
                      old + 1
                    }(tx)
//                    println(threadId + " leaving")
                  }
//                  println(threadId + " left")
                  Thread.`yield`()
                }
              } catch {
                case e: Throwable => throw new InvocationTargetException(e)
              }
            }(pool)
          }
//          println("awaiting completion of threads for " + h)
          inTxnThreads.foreach(tpl => try {
            Await.ready(tpl._2, timeoutDuration)
          } catch {
            case e: TimeoutException => System.err.println(tpl._1 + ": " + e.getMessage())
          })
          val results = inTxnThreads.map(tpl => tpl._1 -> tpl._2.value.get)
          val unpackedResults = results.collect { case (threadId, Failure(exception)) => threadId -> Failure(exception.asInstanceOf[InvocationTargetException].getTargetException()); case other => other }
          System.err.println("results for " + h + ": " + unpackedResults)
          unpackedResults.collect { case (threadId, Failure(exception)) => throw exception }
        }
      } catch {
        case e: Exception => e.printStackTrace(System.err)
      }
    }(pool)

  }).foreach(Await.ready(_, timeoutDuration * numberConcurrentTransactions))

  System.err.println("Count should be " + expected + ": " + count.single.get)
}

object UnsynchronizedTransformTest {
  val v1 = Ref(5)
  def go() = {
    println(v1.single.get)
    atomic { tx =>
      val f1 = Future { v1.transform(v => v + 1)(tx) }
      val f2 = Future { v1.transform(v => v + 1)(tx) }
      Await.ready(f1, Duration(5, SECONDS))
      Await.ready(f2, Duration(5, SECONDS))
    }
    println(v1.single.get)
  }
}
