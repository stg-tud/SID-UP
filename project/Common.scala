import sbt._
import Keys._

object Common {
  def scalaVersion = "2.10.4"
  def version = "0.0.1"
  def organization = "de.tuda.stg"

  def scalacOptions = (
    "-deprecation" ::
    "-encoding" :: "UTF-8" ::
    "-unchecked" ::
    "-feature" ::
    "-target:jvm-1.6" ::
    //"-language:implicitConversions" ::
    //"-language:reflectiveCalls" ::
    "-Xlint" ::
    "-Xfuture" ::
    "-Xlog-implicits" ::
    Nil)

  def libraryDependencies = (
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1" ::
    "org.slf4j" % "slf4j-simple" % "1.7.5" ::
    "org.scalatest" %% "scalatest" % "2.1.7" % "test" ::
    "org.scala-stm" %% "scala-stm" % "0.7" ::
    Nil)

  def javaOptions = (
  "-server" ::
    //"-verbose:gc" ::
    //"-Xms512M" ::
    //"-Xmx512M" ::
    //"-XX:NewRatio=1" ::
    //"-XX:CompileThreshold=100" ::
    //"-XX:+PrintCompilation" ::
    //"-XX:+PrintGCDetails" ::
    //"-XX:+UseParallelGC" ::
    Nil)

  def initialCommands = """
import reactive._
import reactive.signals._
import reactive.events._
"""
}
