package crud.server

import crud.data.Order
import reactive.events.EventStream
import reactive.signals.Signal

@remote trait RemoteCrud {
  def connectInsert(insertEvents: EventStream[Set[Order]]): Unit
  def connectRemove(removeEvents: EventStream[Set[Order]]): Unit
  def select(): Signal[Set[Order]]
  def select(where: Order => Signal[Boolean]): Signal[Set[Order]]
}
