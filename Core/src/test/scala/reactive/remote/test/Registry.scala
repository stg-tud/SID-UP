package reactive.remote.test

object Registry {
  private lazy val _registry = java.rmi.registry.LocateRegistry.createRegistry(1099)
  def requireRegistry = _registry
}