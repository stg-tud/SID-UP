package reactive
import java.util.UUID

case class Transaction(sources: scala.collection.Set[UUID], uuid: UUID = UUID.randomUUID())
