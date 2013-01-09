package test
import reactive.Var
import reactive.Signal
import reactive.Reactive.autoSignalToValue
import reactive.Transaction

object TransactionTest extends App {
	val var1 = Var(1);
	val var2 = Var(5);
	
	val sum = Signal(var1, var2) { var1 + var2 }
	val sumLog = new ReactiveLog(sum);
	
	var1.set(4);
	var2.set(4);
	
	val transaction = new Transaction();

	transaction.set(var1, 5);
	transaction.set(var2, 5);
	transaction.commit();
	
	transaction.set(var1, 2);
	transaction.set(var2, -2);
	transaction.commit();

	var2.set(4);
	
	sumLog.assert(6, 9, 8, 10, 0, 6)
}