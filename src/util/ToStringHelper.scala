package util
import scala.collection.mutable.MutableList
import util.Util._

object ToStringHelper {
  def listTag[A](builder: StringBuilder, depth: Int, tagname: String, elements: MutableList[A]) = new {
    def apply(op: A => Unit) = {
      indent(builder, depth).append("<").append(tagname);
      if (elements.isEmpty) {
        builder.append("/>\n");
      } else {
        builder.append(">\n");
        elements.foreach(op)
        indent(builder, depth).append("</").append(tagname).append(">\n");
      }
    }
  }

  def indent(builder: StringBuilder, depth: Int): StringBuilder = {
    depth.times {
      builder.append("\t");
    }
    return builder;
  }
}