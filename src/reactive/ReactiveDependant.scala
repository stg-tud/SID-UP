package reactive

import util.TicketAccumulator

trait ReactiveDependant[-N] {
	def notify(replyChannel : TicketAccumulator.Receiver, notification : N)
}