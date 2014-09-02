name := "sidup"

lazy val root = project.in(file(".")).aggregate(
	core, unoptimized, benchmark)

lazy val core = project in file("Core")

lazy val unoptimized = project in file("Unoptimized")

lazy val benchmark = project.in(file("Benchmark")).dependsOn(core, unoptimized)

scalaVersion in ThisBuild := "2.11.2"

version in ThisBuild := "0.0.1"

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
