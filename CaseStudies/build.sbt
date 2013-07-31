name := "ReactivePlaygroundCaseStudies"

version := "0.0.0"

scalaVersion := "2.10.2"

scalacOptions ++= List(
	"-deprecation",
	"-encoding", "UTF-8",
	"-unchecked",
	"-feature",
	"-target:jvm-1.6",
	"-Xlint"
)

// resolvers ++= Seq()

libraryDependencies ++= Seq(
	"org.scalatest" %% "scalatest" % "2.0.M5b" % "test",
	"org.slf4j" % "slf4j-simple" % "1.7.5",
	"com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
)
