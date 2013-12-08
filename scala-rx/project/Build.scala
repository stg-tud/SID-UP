import sbt._
import Keys._

object ScalaRx extends Build {

	lazy val scalaRx = Project(
		id = "scala-rx",
		base = file(".")
	)
	.dependsOn(simulateWork)

	lazy val simulateWork = ProjectRef(base = file("../SimulateWork"), id = "SimulateWork")

}
