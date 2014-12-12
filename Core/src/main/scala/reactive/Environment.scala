package reactive

import reactive.signals.{Signal, Var}

object Environment extends App {
  lazy val timestamp: Signal[Long] = {
    val millis = Var(System.currentTimeMillis())
    val thread = new Thread(new Runnable() {
      def run() = {
        while (true) {
          while (millis.now < System.currentTimeMillis()) {
            millis << millis.now + 1
          }
          Thread.sleep(1)
        }
      }
    }, "millis")
    thread.setDaemon(true)
    thread.start()
    millis
  }
}
