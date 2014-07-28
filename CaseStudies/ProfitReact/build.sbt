name := "sidup-casestudy-profitreact"

libraryDependencies ++= Seq(
  "com.storm-enroute" %% "scalameter" % "0.6",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
import projections._
"""
