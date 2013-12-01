name := "scala-react"

organization := "github.com.ingoem"

version := "1.0-simnet"

scalaVersion := "2.10.3"

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.3")

scalacOptions ++= Seq(
	"-deprecation",
	"-unchecked",
	"-P:continuations:enable"
)

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "1.9" % "test",
	"junit" % "junit" % "4.11" % "test"
)
