organization  := "com.rx"

name          := "scalarx"

version       := "0.1"

scalaVersion := "2.10.3"

scalacOptions ++= List(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-target:jvm-1.6",
  "-Xlint"
)

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test",
    "com.typesafe.akka" %% "akka-actor" % "2.1.2"
)

