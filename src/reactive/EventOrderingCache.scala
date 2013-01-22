package reactive

import scala.collection.mutable
import java.util.UUID

abstract class EventOrderingCache[T](initialLastEvents: Map[UUID, UUID]) {
  private val lastEvents = mutable.Map[UUID, UUID]()
  lastEvents ++= initialLastEvents

  private case class EventRecord(event: Event, missingPredecessors: mutable.Set[UUID], data: T);
  private val suspendedRecords = mutable.Map[UUID, List[EventRecord]]()

  def eventReady(event: Event, data: T) = {
    suspendedRecords.synchronized {
      val record = new EventRecord(event, calculateMissingPredecessors(event), data);
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
  }

  private def calculateMissingPredecessors(event: Event) = {
    val result = mutable.Set[UUID]()
    event.sourcesAndPredecessors.foreach {
      case (source, predecessor) =>
        lastEvents.get(source) match {
          case Some(lastEvent) if (predecessor.equals(lastEvent)) =>
          // predecessor requirement already fulfilled, so don't record it
          case None =>
          // won't receive inputs from this source, so don't record it
          case _ =>
            // predecessor not available or different than required => record requirement
            result += predecessor
        }
    }
    result
  }

  private val publishQueue = mutable.Queue[EventRecord]()
  private def publish(newRecord: EventRecord) {
    publishQueue += newRecord;

    while (!publishQueue.isEmpty) {
      val record = publishQueue.dequeue()
      record.event.sourcesAndPredecessors.keysIterator.foreach { source =>
        lastEvents += (source -> record.event.uuid)
      }
      eventReadyInOrder(record.event, record.data)

      suspendedRecords.remove(record.event.uuid).flatten.foreach[Unit] { suspendedRecord =>
        if (suspendedRecord.missingPredecessors.size == 1) {
          publishQueue += suspendedRecord
        } else {
          suspendedRecord.missingPredecessors -= record.event.uuid
        }
      }
    }
  }

  protected[this] def eventReadyInOrder(event: Event, data: T)
}

object EventOrderingCache {
  def intersectionMap[K, V1, V2](map1: scala.collection.Map[K, V1], map2: collection.Map[K, V2]): Map[K, (V1, V2)] = {
    map1.toMap.filterKeys(map2.isDefinedAt(_)).map { case (key, value) => key -> (value, map2(key)) }
  }
}