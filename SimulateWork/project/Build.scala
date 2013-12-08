import sbt._
import Keys._

object SimulateWork extends Build {

  lazy val simulateWork = Project(
    id = "SimulateWork",
    base = file(".")
  )

}
