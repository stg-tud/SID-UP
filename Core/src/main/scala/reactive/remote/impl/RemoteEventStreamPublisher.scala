package reactive.remote.impl

import reactive.signals.Signal
import reactive.remote.RemoteSignalDependency
import reactive.Transaction
import reactive.events.{EventStream, EventSource}

class RemoteEventStreamPublisher[P](val dependency: EventStream[P]) extends RemotePublisher[P] {}
