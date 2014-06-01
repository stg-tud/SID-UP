name := "sidup-casestudy-sharedreactivewhiteboard"

organization := Common.organization

version := Common.version

scalaVersion := Common.scalaVersion

scalacOptions ++= Common.scalacOptions

javaOptions ++= Common.javaOptions

libraryDependencies ++= Common.libraryDependencies

initialCommands in console := """
import reactive._
import reactive.signals._
import reactive.events._
import reactive.remote._
import projections._
"""
