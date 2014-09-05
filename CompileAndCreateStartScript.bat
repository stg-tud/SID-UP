@echo off
sbt clean compile "project benchmark" "set mainClass in Compile := Some(""benchmark.Benchmark"")" start-script
