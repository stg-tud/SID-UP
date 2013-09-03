package reactive
package impl
package mirroring

trait ReactiveNotificationDependant[-P] {
  def fire(notification: ReactiveNotification[P]): Unit
}