import sbt._
import Keys._

object CaseStudy extends Build {

  lazy val caseStudy = Project(
    id = "sidup-casestudy-philosophers",
    base = file(".")
  )
  .dependsOn(core)

  lazy val core = ProjectRef(base = file("../../Core"), id = "sidup-core")
}
