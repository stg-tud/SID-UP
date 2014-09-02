package unoptimized.remote.impl

import unoptimized.signals.Signal
import unoptimized.remote.RemoteSignalDependency
import unoptimized.Transaction
import unoptimized.events.{EventStream, EventSource}

class RemoteEventSourceImpl[P](val dependency: EventStream[P]) extends RemoteSourceImpl[P] {}
