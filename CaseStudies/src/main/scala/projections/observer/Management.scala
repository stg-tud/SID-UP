package projections.observer

trait Management extends Observable[Int] with Observer[Message[Int]] {

  var disableTransaction = false

  var lastSales = 0
  var lastPurchases = 0
  var hasReceived = ""
  var difference: Int = 0

  def init(): Any
  def deinit(): Any

  def recalcDifference() = {
    difference = lastSales - lastPurchases
    notifyObservers(difference)
  }

  def receive(v: Message[Int]) = {
    v.sender match {
      case "purchases" => lastPurchases = v.value
      case "sales" => lastSales = v.value
    }

    (v.direct || disableTransaction) match {
      case true => recalcDifference()
      case false =>
        if (hasReceived == v.sender) throw new Exception("received from same source twice")
        if (hasReceived == "") hasReceived = v.sender
        else {
          hasReceived = ""
          recalcDifference()
        }
    }

  }
}
