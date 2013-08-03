import scala.sys.process._


sealed trait Event

case class Edge(source: Node, sink: Node) extends Event {
	override def toString = s""""${source.id}" -> "${sink.id}";"""
}
case class EdgeDelete(edge: Edge) extends Event {
		override def toString = s""""${edge.source.id}" -> "${edge.sink.id}" [style="dashed"];"""
}
case class Node(id: String, name: String) extends Event {
	override def toString = s""""$id" [label = "$name"];"""
}
case class Pulse(source: Node, value: String, transaction: Transaction) extends Event
case class Transaction(id: String) extends Event

object log2dot {

	val nodeString = """
		\s*
		(\w+)    # node name
		\(
		([^\)]+) # node ID 
		\)\s*
		"""

	val extractEdge = ("""(?x)
		# an edge statement looks something like this
		# LOGLEVEL name(id) <~ name(id) [transaction]
		\w+\s+        # loglevel""" +
		nodeString + """
		(<!?~)\s*  # edge type (deleted or created)""" +
		nodeString + """
		\s*\S+        # transaction 
		""").r
	val extractTransaction = ("""(?x)
		# TRACE start Transaction(id=2e7b9e1e-206e-47ab-a473-6ebcf92064ff,sources=TreeSet(1eeb5d45-babb-4581-b938-990c906d2151))
		\w+\s+              # loglevel
		(start|finish)      # type
		\s*Transaction\(id=
		([^,]+)             # id
		.*                  #the rest
		""").r
	val extractPulse = ("""(?x)
		# TRACE MapSignal(654474800) => Pulse(Some(calculating), false) [Some(d6b1f772-9a9b-4f17-b7cd-1201f2cd3157)]
		\w+\s+              # loglevel""" +
		nodeString + """
		=> \s+ Pulse \(
		(?:None|Some\((.+)\))            # pulse value
		,.*\)\s+
		\[ Some \(
		([^\)]+)            # transaction id
		\)\].*
		""").r

	def writeToFile(p: String)(s: String) {
	  val pw = new java.io.PrintWriter(new java.io.File(p))
	  try {
	    pw.write(s)
	  } finally {
	    pw.close()
	  }
	}

	def extract(line: String): Seq[Event] = line match {
		case extractEdge(name1, id1, delete, name2, id2) => {
			val sink = Node(id1, name1)
			val source = Node(id2, name2)
			val edge = Edge(source, sink)
			if (delete == "<~")
				Seq(sink, source, edge)
			else 
				Seq(EdgeDelete(edge))
		}
		case extractTransaction(start, id) => Seq(Transaction(id))
		case extractPulse(nodeName, nodeID, value, transaction) => {
			val node = Node(nodeID, nodeName)
			Seq(node, Pulse(node, value, Transaction(transaction)))
		}
		case _ => Seq()
	}

	def getEvents(file: String) = { for {
		line <- scala.io.Source.fromFile(file).getLines()
		element <- extract(line)
		} yield element}.toSeq

	def dot(events: Seq[Event], labels: Map[Node, String] = Map()) = {
		val nodes = events.collect{case n: Node => n}.toSet
		val deletedEdges = events.collect{case e: EdgeDelete => e}.toSet
		val edges = events.collect{case e: Edge => e}.toSet -- deletedEdges.map{_.edge}

		def labelledEdge(e: Edge) = labels.get(e.source) match {
			case Some(label) => f""""${e.source.id}%s" -> "${e.sink.id}%s" [label="${label}%.40s"];"""
			case None => e.toString
		}

		val labelledEdges = edges.map{labelledEdge(_)}

		(Seq("digraph Net {") ++ nodes ++ deletedEdges ++ labelledEdges ++ Seq("}")).mkString("\n")
	}

	def getPulses(events: Seq[Event]): Map[Transaction, Map[Node, String]] = {
		val pulses = events.collect{case p: Pulse => p}
		// groups the pulses by transaction, then makes a list of Node -> Value
		// pairs out of the pulses, from which a map is generated
		pulses.groupBy(_.transaction).mapValues{_.zipWithIndex.map{case (p, i) => p.source -> s"$i: ${p.value}"}.toMap}
	}

	def main(args: Array[String]) {

		val infile = args(0) + ".log"
		val tmpfile = "tmp.dot"
		val outfile = args(0) + ".png"

		val events = getEvents(infile)

		val pulses = getPulses(events)

		var i = 1;

		for (transaction <- events.collect{case t: Transaction => t}.distinct) {
			val dotString = dot(events.takeWhile(_ != transaction), pulses(transaction))
			Seq("dot", "-Tpng", "-o", i.toString + outfile) run new ProcessIO(in => {in.write(dotString.getBytes); in.close()}, _.close(), _.close())
			i += 1;
		}

	}
}
