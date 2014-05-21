package reactive

import java.util.UUID
import reactive.signals.Signal
import scala.concurrent.stm.InTxn

trait Reactive[+O, +P] extends Reactive.Dependency {
  protected[reactive] def pulse(tx: InTxn): Reactive.PulsedState[P]
  protected[reactive] def hasPulsed(tx: InTxn): Boolean

  def single: Reactive.View[O]
  def log(implicit inTxn: InTxn): Signal[Seq[O]]
  def observe(obs: O => Unit)(implicit inTxn: InTxn)
  def unobserve(obs: O => Unit)(implicit inTxn: InTxn)
}

object Reactive {
  trait View[+O] {
    protected[reactive] def sourceDependencies: Set[UUID]
    
	def log: Signal[Seq[O]]
    def observe(obs: O => Unit)
    def unobserve(obs: O => Unit)
  }
  
  object PulsedState {
    def apply[X](opt: Option[X]): PulsedState[X] = opt match {
      case None => Reactive.Unchanged
      case Some(x) => Reactive.Changed(x)
    }
  }

  trait PulsedState[+X] {
    def changed: Boolean
    def pulsed: Boolean
    def asOption: Option[X]
  }
  object Pending extends PulsedState[Nothing] {
    override val changed = false
    override val pulsed = false
    override val asOption = None
  }
  object Unchanged extends PulsedState[Nothing] {
    override val changed = false
    override val pulsed = true
    override val asOption = None
  }
  case class Changed[+X](x: X) extends PulsedState[X] {
    override val changed = true
    override val pulsed = true
    override val asOption = Some(x)
  }

  trait Dependant {
    protected[reactive] def ping(transaction: Transaction, sourceDependenciesChanged: Boolean, pulsed: Boolean): Unit
  }
  trait Dependency {
    protected[reactive] def sourceDependencies(inTxn: InTxn): Set[UUID]
    protected[reactive] def isConnectedTo(transaction: Transaction): Boolean
    protected[reactive] def addDependant(tx: InTxn, dependant: Dependant): Unit
    protected[reactive] def removeDependant(tx: InTxn, dependant: Dependant): Unit
  }
  //  type RSeq[+A] = Reactive[Seq[A], Seq[A], Delta[A]]
  //  type Signal[+A] = Reactive[A, A, Update[A]]
  //  implicit def richSignal[A](signal: Signal[A]) = new Object {
  //    lazy val changes: EventStream[A] = new ChangesEventStream(this)
  //    def map[B](op: A => B): Signal[B] = new MapSignal(this, op)
  //    def flatMap[B](op: A => Signal[B]): Signal[B] = map(op).flatten
  //    def flatten[B](implicit evidence: A <:< Signal[B]): Signal[B] = new FlattenSignal(this.asInstanceOf[Signal[Signal[B]]]);
  //    def log = new FoldSignal(List(now), changes, ((list: List[A], elem: A) => list :+ elem));
  //    def snapshot(when: EventStream[_]): Signal[A] = pulse(when).hold(now);
  //    def pulse(when: EventStream[_]): EventStream[A] = new PulseEventStream(this, when);
  //  }
  //  type EventStream[+A] = Reactive[A, Unit, Option[A]]
  //  implicit def richEventStream[A](eventStream: EventStream[A]) = new Object {
  //    def hold[B >: A](initialValue: B): Signal[B] = fold(initialValue){ (_, value) => value }
  //    def map[B](op: A => B): EventStream[B] = new MappedEventStream(this, op);
  //    def merge[B >: A](streams: EventStream[B]*): EventStream[B] = new MergeStream(this :: streams.toList);
  //    def fold[B](initialValue: B)(op: (B, A) => B): Signal[B] = new FoldSignal(initialValue, this, op);
  //    def log = fold(List[A]())((list, elem) => list :+ elem)
  //    def filter(op: A => Boolean): EventStream[A] = new FilteredEventStream(this, op);
  //  }
}