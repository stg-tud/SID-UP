organization := "de.tuda.stg"

name := "sidup-core"

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
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.scala-lang" %% "scala-actors-migration" % "1.0.0",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
)
