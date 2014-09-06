name := "sidup"

lazy val root = project.in(file(".")).aggregate(
	core, ui, log2dot, profit, whiteboard, philosophers)

lazy val core = project in file("Core")

lazy val log2dot = project in file("log2dot")

lazy val ui = project.in(file("Ui")).dependsOn(core)

lazy val profit = project.in(file("CaseStudies/ProfitReact")).dependsOn(ui)

lazy val whiteboard = project.in(file("CaseStudies/SharedReactiveWhiteboard")).dependsOn(ui)

lazy val philosophers = project.in(file("CaseStudies/Philosophers")).dependsOn(core)

scalaVersion in ThisBuild := "2.11.2"

version in ThisBuild := "0.1.1-STM"

organization in ThisBuild := "de.tuda.stg"

scalacOptions in ThisBuild ++= (
  "-deprecation" ::
  "-encoding" :: "UTF-8" ::
  "-unchecked" ::
  "-feature" ::
  "-target:jvm-1.7" ::
  "-Xlint" ::
  "-Xfuture" ::
  //"-Xlog-implicits" ::
  //"-Xfatal-warnings" ::
  "-Yno-adapted-args" ::
  //"-Ywarn-numeric-widen" ::
  //"-Ywarn-value-discard" ::
  "-Ywarn-dead-code" ::
  //"-Yno-predef" ::
  Nil)

libraryDependencies in ThisBuild ++= (
    "com.typesafe.scala-logging" %% "scala-logging" % "3.0.0" ::
    "org.slf4j" % "slf4j-simple" % "1.7.5" % "test" ::
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
