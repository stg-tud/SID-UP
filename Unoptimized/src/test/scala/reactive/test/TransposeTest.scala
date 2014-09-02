package unoptimized.test

import unoptimized.TransactionBuilder
import org.scalatest.FunSuite
import unoptimized.signals.{TransposeSignal, Var}
import unoptimized.events.{TransposeEventStream, EventSource}

class TransposeTest extends FunSuite {
  test("TransposeSignal does something sane") {
    val s1 = Var("s1")
    val s2 = Var("s2")
    val s3 = Var("s3")

    val listSig = Var(Seq(s1, s2, s3))
    val transposed = new TransposeSignal[String](listSig)
    val transposeLog = transposed.log

    assertResult(transposeLog.now) { Seq(Seq("s1", "s2", "s3")) }

    s1 << "s1updated"

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3")
      )
    }(transposeLog.now)

    s3 << "s3updated"

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3"),
        Seq("s1updated", "s2", "s3updated")
      )
    }(transposeLog.now)

    val transaction = new TransactionBuilder
    transaction.set(s2, "s2transaction").set(s3, "s3transaction").commit()

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3"),
        Seq("s1updated", "s2", "s3updated"),
        Seq("s1updated", "s2transaction", "s3transaction")
      )
    }(transposeLog.now)

    listSig << Seq(s2, s3)

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3"),
        Seq("s1updated", "s2", "s3updated"),
        Seq("s1updated", "s2transaction", "s3transaction"),
        Seq("s2transaction", "s3transaction")
      )
    }(transposeLog.now)

    s1 << "s1updatedAgain"

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3"),
        Seq("s1updated", "s2", "s3updated"),
        Seq("s1updated", "s2transaction", "s3transaction"),
        Seq("s2transaction", "s3transaction")
      )
    }(transposeLog.now)

    s2 << "s2updatedAgain"

    assertResult {
      Seq(
        Seq("s1", "s2", "s3"),
        Seq("s1updated", "s2", "s3"),
        Seq("s1updated", "s2", "s3updated"),
        Seq("s1updated", "s2transaction", "s3transaction"),
        Seq("s2transaction", "s3transaction"),
        Seq("s2updatedAgain", "s3transaction")
      )
    }(transposeLog.now)

  }

  test("TransposeEventStream does something sane") {
    val e1, e2, e3 = EventSource[String]

    val listSig = Var(Seq(e1, e2, e3))
    val transposed = new TransposeEventStream[String](listSig)
    val transposeLog = transposed.log

    assertResult { Seq() }(transposeLog.now)

    e1 << "e1fired"

    assertResult {
      Seq(
        Seq("e1fired")
      )
    }(transposeLog.now)

    e3 << "e3fired"

    assertResult {
      Seq(
        Seq("e1fired"),
        Seq("e3fired")
      )
    }(transposeLog.now)

    val transaction = new TransactionBuilder
    transaction.set(e2, "e2fired").set(e3, "e3fired").commit()

    assertResult {
      Seq(
        Seq("e1fired"),
        Seq("e3fired"),
        Seq("e2fired", "e3fired")
      )
    }(transposeLog.now)


    listSig << Seq(e2, e3)

    assertResult {
      Seq(
        Seq("e1fired"),
        Seq("e3fired"),
        Seq("e2fired", "e3fired")
      )
    }(transposeLog.now)

    e1 << "e1firedAgain"

    assertResult {
      Seq(
        Seq("e1fired"),
        Seq("e3fired"),
        Seq("e2fired", "e3fired")
      )
    }(transposeLog.now)

    e2 << "e2firedAgain"

    assertResult {
      Seq(
        Seq("e1fired"),
        Seq("e3fired"),
        Seq("e2fired", "e3fired"),
        Seq("e2firedAgain")
      )
    }(transposeLog.now)

  }
}
