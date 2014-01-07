name := "sidup-casestudy-sharedreactivewhiteboard"

version := "0.0.0"

scalaVersion := "2.10.3"

scalacOptions ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-target:jvm-1.6",
  "-Xlint"
)

javaOptions ++= Seq(
  "-server",
  "-verbose:gc",
  "-Xms512M",
  "-Xmx512M",
  //"-XX:NewRatio=1",
  //"-XX:CompileThreshold=100",
  "-XX:+PrintCompilation",
  "-XX:+PrintGCDetails",
  "-XX:+UseParallelGC"
)

// resolvers ++= Seq()

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
import projections._
"""
