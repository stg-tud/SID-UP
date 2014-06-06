name := "benchmarks"

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Seq(
  "-server",
  //"-verbose:gc",
  "-Xms512M",
  "-Xmx512M",
  "-Xss2M",
  //"-XX:NewRatio=1",
  //"-XX:CompileThreshold=100",
  //"-XX:+PrintCompilation",
  //"-XX:+PrintGCDetails",
  //"-XX:+UseParallelGC",
  ""
)


libraryDependencies ++= Common.libraryDependencies

libraryDependencies ++= Seq(
  "com.github.axel22" %% "scalameter" % "0.4",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
)

initialCommands in console := Common.initialCommands
