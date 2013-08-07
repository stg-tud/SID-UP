package projectionsSockets

import org.scalatest.FunSuite

class SomeTest extends FunSuite {
	test("some basic functionality") {
		val c = new Client("client")
		val s = new Sales()
		val p = new Purchases(5)
		val m = new Management()

		c.startWorking()
		c.makeOrder(5)

		s.startWorking()
		p.startWorking()
		m.startWorking()

		c.makeOrder(10)

		Thread.sleep(2000)
	}
}
