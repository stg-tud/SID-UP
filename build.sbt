name := "sidup"

lazy val root = project.in(file(".")).aggregate(
	// Core
	core,
	// Utilities
	ui, db,
	// Logging or something like that
	log2dot,
	// Case Studies
	profit, whiteboard, crud)

lazy val core = project in file("Core")

lazy val log2dot = project in file("log2dot")

lazy val ui = project.in(file("Ui")).dependsOn(core)
lazy val db = project.in(file("Database")).dependsOn(core)

lazy val profit = project.in(file("CaseStudies/ProfitReact")).dependsOn(ui)
lazy val whiteboard = project.in(file("CaseStudies/SharedReactiveWhiteboard")).dependsOn(ui)
lazy val crud = project.in(file("CaseStudies/SharedCRUD")).dependsOn(ui, db)

scalaVersion in ThisBuild := "2.11.2"

version in ThisBuild := "0.1.1-STM"

organization in ThisBuild := "de.tuda.stg"

scalacOptions in ThisBuild ++= (
    "-deprecation" ::
    "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-feature" ::
    "-target:jvm-1.7" ::
    //"-language:implicitConversions" ::
    //"-language:reflectiveCalls" ::
    "-Xlint" ::
    "-Xfuture" ::
    //"-Xlog-implicits" ::
    Nil)

libraryDependencies in ThisBuild ++= (
    "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0" ::
    "org.slf4j" % "slf4j-simple" % "1.7.5" % "test" ::
    "org.scalatest" %% "scalatest" % "2.1.7" % "test" ::
    Nil)

javaOptions in ThisBuild ++= (
  "-server" ::
    //"-verbose:gc" ::
    //"-Xms512M" ::
    //"-Xmx512M" ::
    //"-XX:NewRatio=1" ::
    //"-XX:CompileThreshold=100" ::
    //"-XX:+PrintCompilation" ::
    //"-XX:+PrintGCDetails" ::
    //"-XX:+UseParallelGC" ::
    Nil)

initialCommands in ThisBuild := """
import reactive._
import reactive.signals._
import reactive.events._
"""
