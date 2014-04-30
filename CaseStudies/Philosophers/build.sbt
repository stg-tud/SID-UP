name := "sidup-casestudy-philosophers"

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
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.scala-stm" %% "scala-stm" % "0.7"
)
