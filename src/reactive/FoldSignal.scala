package reactive

class FoldSignal[A, B](initialValue: A, source: EventStream[B], op: (A, B) => A) extends SignalImpl[A]("fold(" + source.name + ")", initialValue) with StatefulReactiveDependant[B] {
  override def sourceDependencies = source.sourceDependencies;
  source.addDependant(this)

  override def eventReadyInOrder(event: Event, maybeValue: Option[B]) {
    updateValue(event) { currentValue =>
      maybeValue.map { op(currentValue, _) }.getOrElse(currentValue)
    }
  }
}