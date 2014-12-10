package reactive.impl

import reactive.Reactive

trait ReactiveInstanceNameMutable {
  self: Reactive[_, _] =>
  private var name = {
    val classname = getClass.getName
    val unqualifiedClassname = classname.substring(classname.lastIndexOf('.') + 1)

    val trace = Thread.currentThread().getStackTrace();
    var i = 0;
    while (!trace(i).toString().startsWith("reactive.")) i += 1
    while ((trace(i).toString.startsWith("reactive.") && !trace(i).toString().startsWith("reactive.test.")) || trace(i).toString().startsWith("scala.concurrent.stm.")) i += 1

    s"$unqualifiedClassname($hashCode) from ${trace(i)}"
  }

  override def withName(name: String): this.type = {
    this.name = name
    this
  }
  override def toString() = name
}