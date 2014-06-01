name := "sidup"

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Common.javaOptions

libraryDependencies ++= Common.libraryDependencies

initialCommands in console := Common.initialCommands

lazy val root = project.in(file(".")).aggregate(
	core, ui, log2dot, profit, whiteboard, philosophers)

lazy val core = project in file("Core")

lazy val log2dot = project in file("log2dot")

lazy val ui = project.in(file("Ui")).dependsOn(core)

lazy val profit = project.in(file("CaseStudies/ProfitReact")).dependsOn(ui)

lazy val whiteboard = project.in(file("CaseStudies/SharedReactiveWhiteboard")).dependsOn(ui)

lazy val philosophers = project.in(file("CaseStudies/Philosophers")).dependsOn(core)
