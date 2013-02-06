package reactive

object LiftableWrappers {
  val add = ((_:Int) + (_:Int))
  val substract = ((_:Int) - (_:Int))
  val modulo = ((_:Int) % (_:Int))
  val min = (math.min((_:Int), (_:Int)))
  val max = (math.max((_:Int), (_:Int)))
}