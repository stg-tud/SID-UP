import sbt._
import Keys._

object Whiteboard extends Build {

  lazy val whiteboard = Project(
    id = "sidup-casestudy-sharedreactivewhiteboard",
    base = file(".")
  )
  .dependsOn(core, ui)

  lazy val ui = ProjectRef(base = file("../../Ui"), id = "sidup-ui")
  lazy val core = ProjectRef(base = file("../../Core"), id = "sidup-core")

}
