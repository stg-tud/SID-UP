package reactive
package impl

trait Delta[+A]

trait RSeq[+A] extends Reactive[A, Seq, Seq, Delta, RSeq]