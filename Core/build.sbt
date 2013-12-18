organization := "playground"

name := "ReactivePlaygroundCore"

version := "0.0.0"

scalaVersion := "2.10.3"

scalacOptions ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-target:jvm-1.6",
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-Xlint"
)

// resolvers ++= Seq()

libraryDependencies ++= Seq(
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1",
  "com.typesafe.akka" %% "akka-remote" % "2.2.1",
  "org.scala-lang" %% "scala-actors-migration" % "1.0.0",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
"""
