import sbt._
import Keys._

object ScalaReact extends Build {

	lazy val scalaReact = Project(
		id = "scala-react",
		base = file(".")
	)
	.dependsOn(simulateWork)

	lazy val simulateWork = ProjectRef(base = file("../SimulateWork"), id = "SimulateWork")

}
