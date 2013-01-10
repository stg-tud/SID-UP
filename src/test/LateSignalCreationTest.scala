package test
import reactive.Signal
import reactive.Var
import reactive.Reactive.autoSignalToValue

object LateSignalCreationTest extends App {
  val var1 = Var(1);
  val var2 = Var(2);
  val var3 = Var(3);
  val signal = Signal(var1, var2) { var1 + var2 }
  
  var1.set(3);
  var2.set(4);
  var3.set(5);

  val lateSignal = Signal(var3, signal) { var3 + signal }
  val log = new ReactiveLog(lateSignal)
  
  var1.set(2);
  var2.set(2);
  var3.set(2);
  
  log.assert(12, 11, 9, 6);  
}