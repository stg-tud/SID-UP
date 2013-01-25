package reactive

import scala.concurrent.ops.spawn

object Environment extends App {
  lazy val timestamp: Signal[Long] = {
    val millis = Var(System.currentTimeMillis());
    val thread = new Thread(new Runnable() {
      def run {
        while (true) {
          while (millis.asInstanceOf[Signal[Long]].now < System.currentTimeMillis()) {
            millis.set(millis.asInstanceOf[Signal[Long]].now + 1);
          }
          Thread.sleep(1);
        }
      }
    }, "millis");
    thread.setDaemon(true);
    spawn { Thread.sleep(100); thread.start(); }
    millis;
  }
}