package reactive

import scala.collection.mutable
import java.util.UUID

abstract class EventOrderingCache(dependencies: Reactive[_]*) {
  private val lastEvents = mutable.Map[UUID, UUID]()
  dependencies.map { _.sourceDependencies }.flatten.foreach {
    case (source, event) =>
      lastEvents.get(source) match {
        case Some(x) => if (!x.equals(event)) throw new IllegalStateException("Cannot initialize ordering while events are in transit!");
        case None => lastEvents += (source -> event)
      }
  }

  private case class EventRecord(missingPredecessors: mutable.Set[UUID], event: Event);
  private val suspendedRecords = mutable.Map[UUID, List[EventRecord]]()

  def eventReady(event: Event) = {
    val record = new EventRecord(calculateMissingPredecessors(event), event);
    if (record.missingPredecessors.isEmpty) {
      publish(record);
    } else {
      record.missingPredecessors.foreach { predecessor =>
        suspendedRecords += (predecessor -> (suspendedRecords.get(predecessor) match {
          case Some(x) => record :: x
          case _ => List(record)
        }))
      }
    }
  }

  private def calculateMissingPredecessors(event: Event) = {
    val result = mutable.Set[UUID]()
    event.sourcesAndPredecessors.foreach {
      case (source, predecessor) =>
        if (lastEvents.contains(source)) {
          lastEvents.get(source) match {
            case Some(lastEvent) if (predecessor.equals(lastEvent)) =>
            // predecessor requirement already fulfilled, so don't record it
            case _ =>
              // predecessor not available or different than required => record requirement
              result += predecessor
          }
        }
    }
    result
  }

  private val publishQueue = mutable.Queue[EventRecord]()
  def publish(newRecord: EventRecord) {
    publishQueue += newRecord;

    while (!publishQueue.isEmpty) {
      val record = publishQueue.dequeue()
      record.event.sourcesAndPredecessors.keysIterator.foreach { source =>
        lastEvents += (source -> record.event.uuid)
      }
      eventReadyInOrder(record.event)

      suspendedRecords.remove(record.event.uuid).flatten.foreach[Unit] { suspendedRecord =>
        if (record.missingPredecessors.size == 1) {
          publishQueue += record
        } else {
          record.missingPredecessors -= record.event.uuid
        }
      }
    }
  }

  def eventReadyInOrder(event: Event)
}