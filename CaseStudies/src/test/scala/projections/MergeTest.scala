//package projections
//
//import org.scalatest.FunSuite
//import reactive.signals._
//import reactive.Lift._
//
//class SomeTest extends FunSuite {
//	test("some basic functionality") {
//		val week = Var(1)
//		SignalRegistry.register("week", week)
//		val client1 = new Client[Int]("client1")
//		val client2 = new Client[Int]("client2")
//		val purch = new Purchases[Int]()
//		val sales = new Sales[Int]()
//		val manag = new Management[Int]()
//
//		val dontPanic: Boolean => Unit = p => expectResult(false)(p)
//
//		purch.startWorking()
//		sales.startWorking()
//		manag.startWorking()
//		client1.startWorking()
//		client2.startWorking()
//
//		manag.panic.observe(dontPanic)
//
//		expectResult(false)(manag.panic.now)
//		expectResult(0)(manag.difference.now)
//
//		expectResult(0)(purch.total.now)
//		expectResult(0)(sales.total.now)
//
//
//		println("make first order")
//		client1.makeOrder << Order(cost = 10, sales = 20)
//		expectResult(10)(purch.total.now)
//		expectResult(20)(sales.total.now)
//
//
//		println("make changing order")
//		val orderCost = Var(15)
//		val orderSales = Var(20)
//
//		client2.makeOrder << Order(orderCost, orderSales)
//
//		expectResult(25)(purch.total.now)
//		expectResult(40)(sales.total.now)
//
//		orderCost << 25
//
//		expectResult(35)(purch.total.now)
//		expectResult(40)(sales.total.now)
//
//		expectResult(false)(manag.panic.now)
//
//		manag.panic.unobserve(dontPanic)
//		orderCost << 100
//		expectResult(true)(manag.panic.now)
//		client1.makeOrder << Order(1,100)
//		manag.panic.observe(dontPanic)
//
//		client2.makeOrder << Order(1,20)
//		orderCost << 120
//		client1.makeOrder << Order(20,1)
//
//		expectResult(152)(purch.total.now)
//		expectResult(161)(sales.total.now)
//
//		expectResult(false)(manag.panic.now)
//		expectResult(9)(manag.difference.now)
//
//		manag.panic.unobserve(dontPanic)
//		client2.makeOrder << Order(10,0)
//		expectResult(true)(manag.panic.now)
//		client2.makeOrder << Order(1,15)
//		manag.panic.observe(dontPanic)
//
//		val client3 = new Client[Int]("client3")
//		client3.startWorking
//
//		//TODO: this fails, because the additional client will recreate the orders
//		//eventstream, which in turn will cause the fold to be recomputet without
//		//knowledge of the previous values
//		expectResult(5)(manag.difference.now)
//
//	}
//}
