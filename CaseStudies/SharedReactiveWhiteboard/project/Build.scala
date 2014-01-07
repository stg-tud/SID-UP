import sbt._
import Keys._

object Whiteboard extends Build {

	lazy val whiteboard = Project(
		id = "sidup-casestudy-sharedreactivewhiteboard",
		base = file(".")
	)
	.dependsOn(ui)

	lazy val ui = ProjectRef(base = file("../../Ui"), id = "sidup-ui")

}