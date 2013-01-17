package remote
import java.util.UUID
import util.SerializationSafe

case class EstablishSignalConnectionData[A : SerializationSafe](remote : RemoteReactive[A], name : String, value : A, sourceDependencies : Map[UUID, UUID])