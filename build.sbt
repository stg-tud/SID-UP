name := "reactiveplayground"

version := "0.0.0"

scalaVersion := "2.10.2"

scalaSource in Compile <<= baseDirectory {(base) => new File(base, "src")}

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
	"org.scala-lang" %% "scala-actors-migration" % "1.0.0"
)