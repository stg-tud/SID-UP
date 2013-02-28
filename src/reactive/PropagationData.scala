package reactive
import java.util.UUID

case class PropagationData(event : Transaction, newDependencies : List[(UUID, UUID)], obsoleteDependencies : List[(UUID, UUID)])