package elmish
import java.util.UUID

class Transaction(val sources: scala.collection.Set[UUID]) {
  val uuid = UUID.randomUUID();
  override def equals(obj : Any) = {
    obj.isInstanceOf[Transaction] && obj.asInstanceOf[Transaction].uuid.equals(uuid)
  }
  override def hashCode() = {
    uuid.hashCode()
  }
  override def toString() = {
    "Transaction(id="+uuid+",sources="+sources+")"
  }
}
