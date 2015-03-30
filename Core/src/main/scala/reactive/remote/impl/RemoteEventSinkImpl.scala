package reactive.remote.impl

import reactive.remote.RemoteDependency
import reactive.events.impl.EventStreamImpl
import java.io.ObjectStreamException

class RemoteEventSinkImpl[A](dependency: RemoteDependency[A]) extends RemoteSinkImpl[A](dependency) with EventStreamImpl[A] {
  @throws(classOf[ObjectStreamException])
  override protected def writeReplace(): Any = this
}
