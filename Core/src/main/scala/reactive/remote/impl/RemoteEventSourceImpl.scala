package reactive.remote.impl

import reactive.events.EventStream

class RemoteEventSourceImpl[P](val dependency: EventStream[P]) extends RemoteSourceImpl[P] {}
