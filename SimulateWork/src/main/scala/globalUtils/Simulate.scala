package globalUtils


object Simulate {
  var nanobusy = 0L
  var nanosleep = 0L
  var coordinatorsleep = 0L

  def apply(nanos: Long = nanobusy): Long =
    if (nanos > 0) {
      val ct = System.nanoTime()
      var res = 0L
      while (nanos > res) {
        res = System.nanoTime() - ct
      }
      res
    }
    else 0L

  def network(nanos: Long = nanosleep): Long =
    if (nanos > 1000000) {
      val ct = System.nanoTime()
      Thread.sleep((nanos / 1000000).asInstanceOf[Int])
      val slept = System.nanoTime() - ct
      network(nanos - slept)
    }
    else apply(nanos)

  def coordination(nanos: Long = coordinatorsleep) = network(nanos)
}
