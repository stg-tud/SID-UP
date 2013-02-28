package remote

import java.util.UUID
import util.SerializationSafe
import reactive.Signal

case class EstablishSignalConnectionData[A : SerializationSafe](remote : Signal[A], name : String, value : A, sourceDependencies : Map[UUID, UUID])