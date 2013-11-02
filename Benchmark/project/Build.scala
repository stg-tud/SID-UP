import sbt._
import Keys._

object Benchmark extends Build {

	lazy val benchmark = Project(
		id = "Benchmark",
		base = file(".")
	)
//	.dependsOn(core)

//	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")

}
