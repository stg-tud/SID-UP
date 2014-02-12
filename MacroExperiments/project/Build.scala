import sbt._
import Keys._


object BuildSettings {
  val paradiseVersion = "2.0.0-M3"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.scalamacros",
    version := "1.0.0",
    scalaVersion := "2.10.3",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    scalacOptions ++= Seq(),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  )
}

object MyBuild extends Build {
  import BuildSettings._

  lazy val root: Project = Project(
    id = "sidup-macroexperiments",
    base = file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core)
  ) aggregate(macros, core)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      // Not needed in scala 2.11:
      libraryDependencies ++= (
        List("org.scalamacros" % "quasiquotes" % paradiseVersion cross CrossVersion.full)
      )
    )
  )

  lazy val core: Project = Project(
    "core",
    file("core"),
    settings = buildSettings
  ) dependsOn(macros, ui)

  lazy val ui = ProjectRef(base = file("../Ui"), id = "sidup-ui")
}
