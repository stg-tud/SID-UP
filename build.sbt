name := "sidup"

lazy val root = project.in(file(".")).aggregate(
	core, ui, log2dot, profit, whiteboard, philosophers)

lazy val core = project in file("Core")

lazy val log2dot = project in file("log2dot")

lazy val ui = project.in(file("Ui")).dependsOn(core)

lazy val profit = project.in(file("CaseStudies/ProfitReact")).dependsOn(ui)

lazy val whiteboard = project.in(file("CaseStudies/SharedReactiveWhiteboard")).dependsOn(ui)

lazy val philosophers = project.in(file("CaseStudies/Philosophers")).dependsOn(core)

scalaVersion in ThisBuild := "2.10.4"

version in ThisBuild := "0.0.1-STM"

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
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1" ::
    "org.slf4j" % "slf4j-simple" % "1.7.5" ::
    "org.scalatest" %% "scalatest" % "2.1.7" % "test" ::
    "org.scala-stm" %% "scala-stm" % "0.7" ::
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
