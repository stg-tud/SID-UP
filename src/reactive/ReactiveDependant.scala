package reactive

import util.TicketAccumulator
import util.Update
import java.util.UUID

trait ReactiveDependant[-P] {
	def notify(replyChannel : TicketAccumulator.Receiver, notification : ReactiveNotification[P])
}