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
//        println("no missing predecessors => immediate execution for "+record);
        publish(record);
      } else {
//        println("missing predecessors; suspending: "+record);
        record.missingPredecessors.foreach { predecessor =>
          suspendedRecords += (predecessor -> (suspendedRecords.get(predecessor) match {
            case Some(x) => record :: x
            case _ => List(record)
          }))
        }
//        println("updated suspended events: "+suspendedRecords);
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
  private def publish(newRecord: EventRecord) {
    publishQueue += newRecord;

    while (!publishQueue.isEmpty) {
      val record = publishQueue.dequeue()
//      println("publishing "+record);
      record.event.sourcesAndPredecessors.keysIterator.foreach { source =>
        lastEvents += (source -> record.event.uuid)
      }
      eventReadyInOrder(record.event, record.data)

      suspendedRecords.remove(record.event.uuid).flatten.foreach[Unit] { suspendedRecord =>
        if (suspendedRecord.missingPredecessors.size == 1) {
//          println("no more missing predecessors; scheduling "+suspendedRecord);
          publishQueue += suspendedRecord
        } else {
          suspendedRecord.missingPredecessors -= record.event.uuid
//          println("still missing predecessors; re-suspending "+suspendedRecord);
        }
      }
//      println("updated suspended events: "+suspendedRecords);
    }
  }

  protected[this] def eventReadyInOrder(event: Event, data: T)
}