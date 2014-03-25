organization := "de.tuda.stg"

name := "sidup-core"

version := "0.0.0"

scalaVersion := "2.10.4"

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
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.scala-stm" %% "scala-stm" % "0.7"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
//import reactive.remote._
"""
