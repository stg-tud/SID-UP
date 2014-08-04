package reactive

object LiftableWrappers {
  type BinOp = (Int, Int) => Int
  val add: BinOp = _ + _
  val subtract: BinOp = _ - _
  val modulo: BinOp = _ % _
  val min: BinOp = math.min
  val max: BinOp = math.max
  def concat[T] = (a: List[T], b: T) => b :: a
//  val rprint = Lift.signalSink1 { Console.print(_: Any) }
//  val rprintln = Lift.signalSink1 { Console.println(_: Any) }
  def ifThenElse[A] = { (condition: Boolean, ifTrue: A, ifFalse: A) => if (condition) ifTrue else ifFalse }
}
