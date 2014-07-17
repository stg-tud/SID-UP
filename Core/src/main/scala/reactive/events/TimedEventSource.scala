package reactive.events

import java.util.{Date, TimerTask}

import reactive.events.impl.EventSourceImpl

/**
 * a TimedEventSource is used to generate events at certain times
 * @tparam A type of the produced event
 */
trait TimedEventSource[A] extends EventStream[A] {
  /**
   * schedules an event to be produced once
   * @param event the event to be produced
   * @param timestampInMilliseconds timestamp in milliseconds at which the event should be run
   * @see     java.lang.System#currentTimeMillis()
   */
  def schedule(event: A, timestampInMilliseconds: Long): Unit
}

object TimedEventSource {
  lazy val timer = new java.util.Timer()
  def apply[A](): TimedEventSource[A] = new EventSourceImpl[A] with TimedEventSource[A] {
    outer =>
    def schedule(event: A, timestampInMilliseconds: Long) = timer.schedule(new TimerTask {
      override def run(): Unit = outer << event
    }, new Date(timestampInMilliseconds))
  }
}
