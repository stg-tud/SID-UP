package util
/** Witness that a type is safe to serialize. */
trait SerializationSafe[S] extends Serializable
 
object SerializationSafe {
  implicit def serializableIsSafe[S <: Serializable] = new SerializationSafe[S] {}
 
  implicit def valuesAreSafe[A <: AnyVal] = new SerializationSafe[A] {}
}