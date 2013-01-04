package util
/** Witness that a type is safe to serialize. */
trait SerializationSafe[S]
 
object SerializationSafe {
  implicit def serializableIsSafe[S <: Serializable](s: S) = new SerializationSafe[S] {}
 
  implicit def valuesAreSafe[A <: AnyVal] = new SerializationSafe[A] {}
}