package reactive.remote

import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.ObjectStreamException
import java.util.UUID

import reactive.signals.Signal

class SerializableSignal[A] extends Externalizable {

  private var uuid: UUID = _

  def this(dependency: Signal[A]) = {
    this()
    uuid = UUID.randomUUID()
    RemoteReactives.rebind(uuid.toString, dependency)
  }

  def readExternal(o: ObjectInput): Unit = {
    println("read ex")
    uuid = o.readObject().asInstanceOf[UUID]
  }

  @throws(classOf[ObjectStreamException])
  private def readResolve(): AnyRef = {
    println("read resolve")
    RemoteReactives.lookupSignal(uuid.toString)
  }

  def writeExternal(o: ObjectOutput): Unit = {
    println("write ex")
    o.writeObject(uuid)
  }
}

/*
val v1 = Var(5)
val ss = new SerializableSignal[Int](v1)
import java.io._
val oos = new ObjectOutputStream(new FileOutputStream("tmp.stream"))
oos.writeObject(ss)
oos.close()
val ois = new ObjectInputStream(new FileInputStream("tmp.stream"))
val res = ois.readObject().asInstanceOf[Signal[Int]]
ois.close()
*/
