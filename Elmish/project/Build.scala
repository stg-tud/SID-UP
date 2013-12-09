import sbt._
import Keys._

object Elmish extends Build {

	lazy val root = Project(
		id = "Elmish",
		base = file(".")
	)

}
