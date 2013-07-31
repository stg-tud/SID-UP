import sbt._
import Keys._

object CaseStudy extends Build {

	lazy val caseStudy = Project(
		id = "ReactivePlaygroundCaseStudies",
		base = file(".")
	)
	.dependsOn(core)
	.dependsOn(ui)

	lazy val core = ProjectRef(base = file("../Core"), id = "ReactivePlaygroundCore")

	lazy val ui = ProjectRef(base = file("../Ui"), id = "ReactivePlaygroundUi")

}
