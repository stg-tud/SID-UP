name := "sidup"

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Common.javaOptions

libraryDependencies ++= Common.libraryDependencies

initialCommands in console := Common.initialCommands

lazy val root = project.in(file(".")).aggregate(
	core, ui, log2dot, profit, benchmark, simulateWork, elmish, scalarx, scalareact)

lazy val core = project in file("Core")

lazy val log2dot = project in file("log2dot")

lazy val ui = project.in(file("Ui")).dependsOn(core)

lazy val profit = project.in(file("CaseStudies")).dependsOn(ui)

lazy val simulateWork = project.in(file("SimulateWork"))

lazy val elmish = project in file("Elmish")

lazy val scalarx = project.in(file("scala-rx")).dependsOn(simulateWork)

lazy val scalareact = project.in(file("scala-react")).dependsOn(simulateWork)

lazy val benchmark = project.in(file("Benchmark")).dependsOn(ui, core, scalareact, scalarx, elmish)
