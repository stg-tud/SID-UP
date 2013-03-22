package reactive.impl

import scala.collection.mutable
import java.util.UUID
import reactive.Event

abstract class EventOrderingCache[T](initialLastEvents: Map[UUID, UUID]) {
  private val lastEvents = mutable.Map[UUID, UUID]()
  lastEvents ++= initialLastEvents

  private case class EventRecord(event: Event, missingPredecessors: mutable.Set[UUID], data: T);
  private val suspendedRecords = mutable.Map[UUID, List[EventRecord]]()

  def eventReady(event: Event, data: T) = {
    suspendedRecords.synchronized {
      val record = new EventRecord(event, calculateMissingPredecessors(event), data);
      if (record.missingPredecessors.isEmpty) {
        Some(record);
      } else {
        record.missingPredecessors.foreach { predecessor =>
          suspendedRecords += (predecessor -> (suspendedRecords.get(predecessor) match {
            case Some(x) => record :: x
            case _ => List(record)
          }))
        }
        None
      }
    }.foreach { publish(_) }
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

  private def publish(newRecord: EventRecord) {
    var readyEvents = newRecord :: Nil

    while (!readyEvents.isEmpty) {
      val record = readyEvents.head
      readyEvents = readyEvents.tail

      eventReadyInOrder(record.event, record.data)

      suspendedRecords.synchronized {
        record.event.sourcesAndPredecessors.keysIterator.foreach { source =>
          if(lastEvents.contains(source)) lastEvents += (source -> record.event.uuid)
        }

        suspendedRecords.remove(record.event.uuid).foreach{_.foreach[Unit] { suspendedRecord =>
          if (suspendedRecord.missingPredecessors.size == 1) {
            readyEvents = suspendedRecord :: readyEvents;
          } else {
            suspendedRecord.missingPredecessors -= record.event.uuid
          }
        }}
      }
    }
  }

  /**
   *  for temporally related events, this will be invoked in order.
   *  for temporally unrelated events, this can be invoked concurrently.
   */
  protected[this] def eventReadyInOrder(event: Event, data: T)
}

object EventOrderingCache {
  def intersectionMap[K, V1, V2](map1: scala.collection.Map[K, V1], map2: collection.Map[K, V2]): Map[K, (V1, V2)] = {
    map1.toMap.filterKeys(map2.isDefinedAt(_)).map { case (key, value) => key -> (value, map2(key)) }
  }
}