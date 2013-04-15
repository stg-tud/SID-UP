package reactive

trait ReactiveDependant[-N] {
	def notify(notification : N)
}