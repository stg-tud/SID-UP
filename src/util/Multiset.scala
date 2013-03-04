package util

class Multiset[+A] private (private val underlying: scala.collection.Map[A, Int], val size: Int) {
  def apply(value: A) = {
    get(value);
  }
  def get(value: A) = {
    underlying(value);
  }
  def +(value: A, count: Int = 1) = {
    this - (value, -count);
  }

  def -(value: A, count: Int = 1) = {
    set(value, underlying(value) - count);
  }

  def set(value: A, after: Int) = {
    val before = get(value)
    if (after == 0) {
      if (before == 0) {
        this
      } else {
        new Multiset(underlying - value, size - before)
      }
    } else {
      new Multiset(underlying + (value -> after), size + after - before)
    }
  }

  def ++(values: Multiset[A]): Multiset[A] = {
    this ++ values.underlying
  }
  def ++(values: scala.collection.Map[A, Int]): Multiset[A] = {
    values.foldLeft(this) { (aggregate, valueAndCount) => aggregate + (valueAndCount._1, valueAndCount._2) }
  }
  def --(values: Multiset[A]): Multiset[A] = {
    this -- values.underlying
  }
  def --(values: scala.collection.Map[A, Int]): Multiset[A] = {
    values.foldLeft(this) { (aggregate, valueAndCount) => aggregate - (valueAndCount._1, valueAndCount._2) }
  }

  def diff(other: Multiset[A]) = {
    (toSet ++ other.toSet).foldLeft(Multiset[A]()) { (aggregate, element) =>
      aggregate.set(element, this(element) - other(element))
    }
  }

  lazy val signum = Multiset(underlying.mapValues(math.signum(_: Int)))

  val isEmpty = size == 0

  def contains(value: A) = underlying.contains(value)
  def toSet = underlying.keySet

  override def toString = "Multiset" + underlying.toString.substring(3);
}

object Multiset {
  val empty = Multiset[Nothing]()
  def apply[A](values: (A, Int)*): Multiset[A] = new Multiset(Map(values: _*).withDefaultValue(0), values.map(_._2).foldLeft(0)(_ + _))
  def apply[A](map: scala.collection.Map[A, Int]): Multiset[A] = apply(map.toSeq: _*)
}