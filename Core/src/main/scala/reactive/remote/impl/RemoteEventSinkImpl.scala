package reactive.remote.impl

import reactive.events.impl.EventStreamImpl
import reactive.remote.RemoteDependency

class RemoteEventSinkImpl[A](dependency: RemoteDependency[A]) extends RemoteSinkImpl[A](dependency) with EventStreamImpl[A] {
  self =>
  override object single extends EventStreamImpl.ViewImpl[A] with RemoteSinkImpl.ViewImpl[A] {
    override val impl = self
  }
}
