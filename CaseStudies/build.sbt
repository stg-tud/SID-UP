name := "sidup-casestudy-profitreact"

mainClass in (Compile) := Some("projections.benchmark.SimpleBenchmark")

seq(com.typesafe.sbt.SbtNativePackager.packageArchetype.java_application: _*)

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Common.javaOptions

libraryDependencies ++= Common.libraryDependencies

libraryDependencies ++= Seq(
  "com.github.axel22" %% "scalameter" % "0.4-M2",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
import projections._
"""
