import sbt._
import Keys._

object ReactivePlayground extends Build {

	lazy val root = Project(
		id = "ReactivePlayground",
		base = file(".")
	)

}
