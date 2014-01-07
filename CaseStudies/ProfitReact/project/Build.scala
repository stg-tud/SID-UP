import sbt._
import Keys._

object CaseStudy extends Build {

	lazy val caseStudy = Project(
		id = "sidup-casestudy-profitreact",
		base = file(".")
	)
	//.dependsOn(core)
	.dependsOn(ui)

	//lazy val core = ProjectRef(base = file("../../Core"), id = "sidup-core")

	lazy val ui = ProjectRef(base = file("../../Ui"), id = "sidup-ui")

}
