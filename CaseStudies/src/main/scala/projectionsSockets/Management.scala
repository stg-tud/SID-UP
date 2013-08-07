package projectionsSockets

import java.io._
import java.net._

import com.typesafe.scalalogging.slf4j.Logging

class Management extends Logging {

	def thread[F](f: => F) = (new Thread( new Runnable() { def run() { f } } )).start

	var purch: Int = 0
	var sales: Int = 0

	var difference: Int = 0
	def recalcDifference() = {
		logger.debug("management recalculating difference")
		difference = sales - purch
		logger.info(s"new difference is $difference")
	}

	def startWorking() {
		logger.debug("management startet working")

    thread {
    	val psock = new ObjectInputStream(new Socket("127.0.0.1", 27801).getInputStream())
      while (true) {
        logger.debug(s"management reading purchases")
        val v = psock.readObject().asInstanceOf[Option[Int]]
        logger.debug(s"management received $v from purchases")
        synchronized {
        	purch = v.get
        	recalcDifference()
        }
      }
    }

    thread {
    	val ssock = new ObjectInputStream(new Socket("127.0.0.1", 27802).getInputStream())
      while (true) {
        logger.debug(s"management reading sales")
        val v = ssock.readObject().asInstanceOf[Option[Int]]
        logger.debug(s"management received $v from sales")
        synchronized {
        	sales = v.get
        	recalcDifference()
        }
      }
    }

	}
}
