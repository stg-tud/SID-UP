def time[T](desc: String = "")(f: => T): T = {
  val start = System.nanoTime
  val res = f
  println(s"$desc took ${(System.nanoTime - start) / 1000000.0} ms")
  res
}

val start = rx.Var(-1)
val result = rx.Rx { start() + 1 }
Range(0, 100).foreach { _ =>
  time("") {
    var i = 0
    while (i < 100) {
      start() = i
      i += 1
    }
  }
}




































































































