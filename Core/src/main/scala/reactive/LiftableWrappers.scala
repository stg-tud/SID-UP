package reactive

object LiftableWrappers {
  val add = (_: Int) + (_: Int)
  val substract = (_: Int) - (_: Int)
  val modulo = (_: Int) % (_: Int)
  val min = math.min((_: Int), (_: Int))
  val max = math.max((_: Int), (_: Int))
  def concat[T] = (a: List[T], b: T) => b :: a
//  val rprint = Lift.signalSink1 { Console.print(_: Any) }
//  val rprintln = Lift.signalSink1 { Console.println(_: Any) }
  def ifThenElse[A] = { (condition: Boolean, ifTrue: A, ifFalse: A) => if (condition) ifTrue else ifFalse }
}