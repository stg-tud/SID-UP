package reactive

import java.util.UUID
import util.Update

trait ReactiveNotification[+T]{
  val transaction : Transaction
  val sourceDependenciesUpdate : Update[Set[UUID]]
}