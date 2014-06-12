name := "sidup-casestudy-profitreact"

libraryDependencies ++= Seq(
  "com.github.axel22" %% "scalameter" % "0.4-M2",
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
)

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
import projections._
"""
