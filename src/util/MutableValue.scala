package util

class MutableValue[A](private var value: A) {
  private var _noChangeUpdate: Update[A] = null

  def current() = value
  def update(newValue: A) = {
    val oldValue = value
    value = newValue
    _noChangeUpdate = null
    new Update(oldValue, newValue, oldValue != newValue)
  }
  
  def transform(op : A => A) = {
    update(op(current()))
  }
  
  def noChangeUpdate = {
    if (_noChangeUpdate == null) {
      _noChangeUpdate = new Update(value, value, false);
    }
    _noChangeUpdate
  }
}

case class Update[+A](val oldValue: A, val newValue: A, changed: Boolean) {
  lazy val newValueIfChanged = if (changed) Some(newValue) else None
  def applyTo[B >: A](victim: MutableValue[B]) = if (changed) victim.update(newValue) else this
  def applyToMapped[B](victim: MutableValue[B], op: A => B) = if (changed) victim.update(op(newValue)) else victim.noChangeUpdate
}