package reactive

import scala.concurrent._
import ExecutionContext.Implicits.global
import reactive.signals.Var
import reactive.signals.Signal

object Environment extends App {
  lazy val timestamp: Signal[Long] = {
    val millis = Var(System.currentTimeMillis());
    val thread = new Thread(new Runnable() {
      def run: Unit = {
        while (true) {
          while (millis.asInstanceOf[Signal[Long]].now < System.currentTimeMillis()) {
            millis << millis.asInstanceOf[Signal[Long]].now + 1;
          }
          Thread.sleep(1);
        }
      }
    }, "millis");
    thread.setDaemon(true);
    Future { Thread.sleep(100); thread.start(); }
    millis;
  }
}