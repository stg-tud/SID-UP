organization := "de.tuda.stg"

name := "sidup-core"

version := "0.0.0"

scalaVersion := "2.11.0"

incOptions := incOptions.value.withNameHashing(true)

scalacOptions ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-target:jvm-1.6",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-Xlint",
  "-Xfuture"
)

// resolvers ++= Seq()

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.scalatest" %% "scalatest" % "2.1.3" % "test",
  "org.scala-stm" %% "scala-stm" % "0.7"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
//import reactive.remote._
"""
