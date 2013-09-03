package reactive
package impl
package mirroring

import java.util.UUID

case class ReactiveNotification[+P](transaction: Transaction, pulse: Option[P], sourceDependencies: Option[Reactive.Topology])