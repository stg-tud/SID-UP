package reactive
package events

import java.util.UUID
import util.Update

case class EventNotification[+A] (transaction : Transaction, sourceDependenciesUpdate : Update[Set[UUID]], maybeValue : Option[A]) extends ReactiveNotification[A]
