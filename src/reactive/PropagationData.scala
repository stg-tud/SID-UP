package reactive
import java.util.UUID

case class PropagationData(event : Event, newDependencies : List[(UUID, UUID)], obsoleteDependencies : List[(UUID, UUID)])