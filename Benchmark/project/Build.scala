import sbt._
import Keys._

object Benchmark extends Build {

	lazy val benchmark = Project(
		id = "Benchmark",
		base = file(".")
	)
	.dependsOn(core)
	.dependsOn(elmish)
	.dependsOn(scalaReact)
  .dependsOn(scalaRx)
  .dependsOn(simulateWork)

  lazy val scalaRx = ProjectRef(base = file("../scala-rx"), id = "scala-rx")
  lazy val simulateWork = ProjectRef(base = file("../SimulateWork"), id = "SimulateWork")
	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")
	lazy val scalaReact = ProjectRef(base = file("../scala-react"), id = "scala-react")
	lazy val elmish = ProjectRef(base = file("../Elmish"), id = "Elmish")

}
