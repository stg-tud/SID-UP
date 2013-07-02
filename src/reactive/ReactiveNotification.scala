package reactive

import util.Update;
import java.util.UUID

case class ReactiveNotification[+P](transaction : Transaction, sourceDependenciesUpdate : Update[Set[UUID]], pulse : P)