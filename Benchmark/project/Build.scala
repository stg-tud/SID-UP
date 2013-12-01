import sbt._
import Keys._

object Benchmark extends Build {

	lazy val benchmark = Project(
		id = "Benchmark",
		base = file(".")
	)
	.dependsOn(core)
	.dependsOn(scalaReact)

	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")
	lazy val scalaReact = ProjectRef(base = file("../scala-react"), id = "scala-react")

}
