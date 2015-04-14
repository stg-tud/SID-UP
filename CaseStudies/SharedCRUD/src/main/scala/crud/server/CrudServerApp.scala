package crud.server

import java.rmi.Naming
import java.rmi.server.UnicastRemoteObject
import java.text.SimpleDateFormat

import crud.data.Order
import db.Table
import reactive.events.EventStream
import reactive.signals.Signal

object CrudServerApp extends App{
  // Setup Table
  val format = new SimpleDateFormat("yyy-MM-dd")
  val table = Table[Order](
    Order(1, format.parse("2015-01-01")),
    Order(2, format.parse("2015-01-02")),
    Order(3, format.parse("2015-01-03"))
  )

  object remoteCrudImpl extends UnicastRemoteObject with RemoteCrud {
    override def connectInsert(insertEvents: EventStream[Set[Order]]): Unit = {
      table.insertEvents << table.insertEvents.now + insertEvents
    }

    override def connectRemove(removeEvents: EventStream[Set[Order]]): Unit = {
      table.removeEvents << table.removeEvents.now + removeEvents
    }

    override def select(): Signal[Set[Order]] = {
      table.select()
    }

    override def select(where: (Order) => Signal[Boolean]): Signal[Set[Order]] = {
      table.select(where)
    }
  }

  try { java.rmi.registry.LocateRegistry.createRegistry(1099) }
  catch { case _: Exception => println("registry already initialised") }
  Naming.rebind("remoteCrud", remoteCrudImpl)
}
