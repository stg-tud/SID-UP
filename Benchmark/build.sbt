name := "Benchmark"

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
  //"-verbose:gc",
  "-Xms512M",
  "-Xmx512M",
  "-Xss100M",
  //"-XX:NewRatio=1",
  //"-XX:CompileThreshold=100",
  //"-XX:+PrintCompilation",
  //"-XX:+PrintGCDetails",
  //"-XX:+UseParallelGC",
  ""
)

// resolvers ++= Seq()

libraryDependencies ++= Seq(
  "com.github.axel22" %% "scalameter" % "0.4-M2",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2",
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "github.com.ingoem" %% "scala-react" % "latest.integration",
  "playground" %% "reactiveplaygroundcore" % "latest.integration",
  "com.scalarx" %% "scalarx" % "0.1"
)

initialCommands in console := """
def time[T](desc: String = "")(f: => T): T = {
val start = System.nanoTime
val res = f
println(s"$desc took ${(System.nanoTime - start) / 1000000.0} ms")
res
}
"""
