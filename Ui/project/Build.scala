import sbt._
import Keys._

object UI extends Build {

	lazy val ui = Project(
		id = "ReactivePlaygroundUi",
		base = file(".")
	).dependsOn(core)

	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")

}
