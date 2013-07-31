import sbt._
import Keys._

object ReactivePlaygroundTests extends Build {

	lazy val root = Project(
		id = "ReactivePlaygroundTests",
		base = file(".")
	).dependsOn(core)

	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")

}
