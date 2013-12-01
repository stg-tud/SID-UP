import sbt._
import Keys._

object ScalaReact extends Build {

	lazy val scalaReact = Project(
		id = "scala-react",
		base = file(".")
	)
//	.dependsOn(core)

//	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")

}
