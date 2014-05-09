package reactive.remote.impl

import reactive.remote.RemoteDependency
import reactive.events.impl.EventStreamImpl

class RemoteEventSinkImpl[A](dependency: RemoteDependency[A]) extends RemoteSinkImpl[A](dependency) with EventStreamImpl[A] {
  self =>
  override object single extends EventStreamImpl.ViewImpl[A] with RemoteSinkImpl.ViewImpl[A] {
    override val impl = self
  }
}
