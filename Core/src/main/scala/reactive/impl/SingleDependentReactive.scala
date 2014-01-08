package reactive
package impl

import java.util.UUID

import scala.language.higherKinds
trait SingleDependentReactive[X, +OW[+_], +VW[+_], +PW[+_], +R[+Y] <: Reactive[Y, OW, VW, PW, R]] {
  self: DependentReactive[X, OW, VW, PW, R] =>

  protected val dependency: DependableReactive
  dependency.addDependant(null, this)

  override def apply(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean) {
    synchronized {
      doReevaluation(transaction, sourceDependenciesChanged, pulsed)
    }
  }

  protected def calculateSourceDependencies(transaction: Transaction) = {
    dependency.sourceDependencies(transaction)
  }
}