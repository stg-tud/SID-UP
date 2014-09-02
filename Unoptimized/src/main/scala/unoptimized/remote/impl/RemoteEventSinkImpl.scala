package unoptimized.remote.impl

import unoptimized.remote.RemoteDependency
import unoptimized.events.impl.EventStreamImpl

class RemoteEventSinkImpl[A](dependency: RemoteDependency[A]) extends RemoteSinkImpl[A](dependency) with EventStreamImpl[A] {}
